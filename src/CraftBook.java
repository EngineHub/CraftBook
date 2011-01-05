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

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.state.StateManager;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

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
    private static final File pathToState = new File("world"+File.separator+"craftbook");
    
    /**
     * Listener for the plugin system. This listener handles configuration
     * loading and the bulk of the core functions for CraftBook. Individual
     * features are implemented in the delegate listeners.
     */
    private final CraftBookListener listener =
            new CraftBookListener(this);
    
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
     * State manager object.
     */
    private StateManager stateManager = new StateManager();
    
    /**
     * State manager thread. 
     */
    private Thread stateThread = new Thread() {
        public void run() {
            PluginLoader l = etc.getLoader();
            while(l.getPlugin("CraftBook")==CraftBook.this) {
                if(l.getPlugin("CraftBook")==CraftBook.this) 
                    try {Thread.sleep(10*60*1000);} catch (InterruptedException e) {}
                if(l.getPlugin("CraftBook")==CraftBook.this) 
                    stateManager.save(pathToState);
            }
        }
    };

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
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        TickPatch.applyPatch();

        registerHook(listener, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(listener, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(listener, "REDSTONE_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(listener, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);

        registerHook(mechanisms, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_RIGHTCLICKED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "SERVERCOMMAND", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(mechanisms);
        
        registerHook(redstone, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(redstone);

        registerHook(vehicle, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "BLOCK_PLACE", PluginListener.Priority.LOW);
        registerHook(vehicle, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_POSITIONCHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_UPDATE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DAMAGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_ENTERED", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DESTROYED", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(vehicle);
        
        TickPatch.addTask(TickPatch.wrapRunnable(this, delay));
        
        pathToState.mkdirs();
        stateManager.load(pathToState);
        
        stateThread.setName("StateManager");
        stateThread.start();
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
        
        SignPatch.applyPatch();
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
                            etc.getLoader().disablePlugin("MinecartMania");
                            logger.warning("Minecart Mania has been disabled.");
                        } finally {
                            etc.getLoader().enablePlugin("CraftBook");
                        }
                    }
                });
                
                return;
            }
        }

        SignPatch.removePatch();
        stateManager.save(pathToState);
        
        listener.disable();
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
        
        Package p = CraftBook.class.getPackage();
        
        if (p == null) {
            p = Package.getPackage("com.sk89q.craftbook");
        }
        
        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();
            
            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
    
    public TickDelayer getDelay() {
        return delay;
    }
    
    public StateManager getStateManager() {
        return stateManager;
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
    
    protected static SignText getSignText(Vector pt) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(pt.getBlockX(),
                pt.getBlockY(), pt.getBlockZ());
        if (cblock instanceof Sign) {
            return new HmodSignTextImpl((Sign)cblock);
        }
        
        return null;
    }

    public static void dropSign(int x, int y, int z) {
        etc.getServer().setBlockAt(0, x, y, z);
        etc.getServer().dropItem(x, y, z, 323);
    }

    public static void dropSign(Vector pt) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
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
