// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.io.*;
import com.sk89q.craftbook.*;

/**
 * Entry point for the plugin for hey0's mod.
 *
 * @author sk89q
 */
public class CraftBook extends Plugin {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    /**
     * Listener for the plugin system. This listener handles configuration
     * loading and the bulk of the core functions for CraftBook. Individual
     * features are implemented in the delegate listeners.
     */
    private final CraftBookListener listener =
            new CraftBookListener(this);
    /**
     * Delegate listener for mechanisms.
     */
    private final CraftBookDelegateListener mechanisms =
            new MechanismListener(this, listener);
    /**
     * Delegate listener for redstone.
     */
    private final CraftBookDelegateListener redstone =
            new RedstoneListener(this, listener);
    /**
     * Delegate listener for vehicle.
     */
    private final CraftBookDelegateListener vehicle =
            new VehicleListener(this, listener);
    
    /**
     * Tick delayer instance used to delay some events until the next tick.
     * It is used mostly for redstone-related events.
     */
    private final TickDelayer delay = new TickDelayer();

    /**
     * Used to fake the data value at a point. For the redstone hook, because
     * the data value has not yet been set when the hook is called, its data
     * value is faked by CraftBook. As all calls to get a block's data are
     * routed through CraftBook already, this makes this hack feasible.
     */
    private static BlockVector fakeDataPos;
    
    /**
     * Used to fake the data value at a point. See fakedataPos.
     */
    private static int fakeDataVal;
    
    /**
     * CraftBook version, fetched from the .jar's manifest. Used to print the
     * CraftBook version in various places.
     */
    private String version;

    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        TickPatch.applyPatch();
        TorchPatch.applyPatch();

        registerHook(listener, "BLOCK_CREATED", PluginListener.Priority.MEDIUM);
        registerHook(listener, "BLOCK_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(listener, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(listener, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(listener, "REDSTONE_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(listener, "COMPLEX_BLOCK_CHANGE", PluginListener.Priority.MEDIUM);

        registerHook(mechanisms, "BLOCK_CREATED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "COMPLEX_BLOCK_CHANGE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(mechanisms);
        
        registerHook(redstone, "COMPLEX_BLOCK_CHANGE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(redstone);

        registerHook(vehicle, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_POSITIONCHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_UPDATE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DAMAGE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(vehicle);
        
        TickPatch.addTask(TickPatch.wrapRunnable(this, delay));
    }

    /**
     * Conditionally registers a hook for a listener.
     * 
     * @param name
     * @param priority
     * @return whether the hook was registered correctly
     */
    public boolean registerHook(PluginListener listener,
    		String name, PluginListener.Priority priority) {
        try {
            PluginLoader.Hook hook = PluginLoader.Hook.valueOf(name);
            etc.getLoader().addListener(hook, listener, this, priority);
            return true;
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "CraftBook: Missing hook " + name + "!");
            return false;
        }
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        logger.log(Level.INFO, "CraftBook version " + getVersion() + " loaded");

        // This will also fire the loadConfiguration() methods of delegates
        listener.loadConfiguration();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
    	StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    	
    	for (StackTraceElement element : elements) {
    		if (element.getClassName().contains("MinecartMania")) {
    			etc.getServer().addToServerQueue(new Runnable() {
    				public void run() {
    					try {
        					logger.warning("Minecart Mania has been disabled.");
    						etc.getLoader().disablePlugin("MinecartMania");
    					} finally {
    						etc.getLoader().enablePlugin("CraftBook");
    					}
    				}
    			});
    			
    			break;
    		}
    	}
    }

    /**
     * Get the CraftBook version.
     *
     * @return
     */
    public String getVersion() {
        if (version != null) {
            return version;
        }
        
        try {
            String classContainer = CraftBook.class.getProtectionDomain()
                    .getCodeSource().getLocation().toString();
            URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes attrib = manifest.getMainAttributes();
            String ver = (String)attrib.getValue("CraftBook-Version");
            version = ver != null ? ver : "(unavailable)";
        } catch (IOException e) {
            version = "(unknown)";
        }

        return version;
    }
    
    public TickDelayer getDelay() {
        return delay;
    }

    protected static int getBlockID(int x, int y, int z) {
        return etc.getServer().getBlockIdAt(x, y, z);
    }

    protected static int getBlockID(Vector pt) {
        return etc.getServer().getBlockIdAt(pt.getBlockX(),
                pt.getBlockY(), pt.getBlockZ());
    }

    protected static int getBlockData(int x, int y, int z) {
        if (fakeDataPos != null
                && fakeDataPos.toBlockVector().equals(new BlockVector(x, y, z))) {
            return fakeDataVal;
        }
        return etc.getServer().getBlockData(x, y, z);
    }

    protected static int getBlockData(Vector pt) {
        if (fakeDataPos != null
                && fakeDataPos.equals(pt.toBlockVector())) {
            return fakeDataVal;
        }
        return etc.getServer().getBlockData(pt.getBlockX(),
                pt.getBlockY(), pt.getBlockZ());
    }

    protected static boolean setBlockID(int x, int y, int z, int type) {
        if (y < 127 && BlockType.isBottomDependentBlock(getBlockID(x, y + 1, z))) {
            etc.getServer().setBlockAt(0, x, y + 1, z);
        }
        return etc.getServer().setBlockAt(type, x, y, z);
    }

    protected static boolean setBlockID(Vector pt, int type) {
        return setBlockID(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type);
    }

    protected static boolean setBlockData(int x, int y, int z, int data) {
        return etc.getServer().setBlockData(x, y, z, data);
    }

    protected static boolean setBlockData(Vector pt, int data) {
        return setBlockData(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data);
    }

    public static void dropSign(int x, int y, int z) {
        etc.getServer().setBlockAt(0, x, y, z);
        etc.getServer().dropItem(x, y, z, 323);
    }

    protected static void fakeBlockData(int x, int y, int z, int data) {
        fakeDataPos = new BlockVector(x, y, z);
        fakeDataVal = data;
    }

    protected static void fakeBlockData(Vector pt, int data) {
        fakeDataPos = pt.toBlockVector();
        fakeDataVal = data;
    }

    protected static void clearFakeBlockData() {
        fakeDataPos = null;
    }
}
