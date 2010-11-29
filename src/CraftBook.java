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
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * Listener for the plugin system.
     */
    private static final CraftBookListener listener =
            new CraftBookListener();

    /**
     * Used to fake the data value at a point.
     */
    private static BlockVector fakeDataPos;
    /**
     * Used to fake the data value at a point.
     */
    private static int fakeDataVal;

    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        PluginLoader loader = etc.getLoader();

        registerHook("BLOCK_CREATED", PluginListener.Priority.MEDIUM);
        registerHook("BLOCK_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook("COMMAND", PluginListener.Priority.MEDIUM);
        registerHook("DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook("COMPLEX_BLOCK_CHANGE", PluginListener.Priority.MEDIUM);

        if (!registerHook("REDSTONE_CHANGE", PluginListener.Priority.MEDIUM)) {
            logger.log(Level.WARNING, "CraftBook: Your version of hMod is "
                    + "does NOT have redstone support! Redstone features will "
                    + "be disabled in CraftBook.");
        }
        
        if (!registerHook("VEHICLE_UPDATE", PluginListener.Priority.MEDIUM)) {
        	logger.log(Level.WARNING, "CraftBook: Your version of hMod "
        			+ "does NOT have vehicle hook support! Minecart-related "
					+ "features will be unavailable.");
        }
    }

    /**
     * Conditionally registers a hook.
     * 
     * @param name
     * @param priority
     * @return where the hook was registered correctly
     */
    public boolean registerHook(String name, PluginListener.Priority priority) {
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

        listener.loadConfiguration();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
    }

    /**
     * Get the CraftBook version.
     *
     * @return
     */
    private String getVersion() {
        try {
            String classContainer = CraftBook.class.getProtectionDomain()
                    .getCodeSource().getLocation().toString();
            URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes attrib = manifest.getMainAttributes();
            String ver = (String)attrib.getValue("CraftBook-Version");
            return ver != null ? ver : "(unavailable)";
        } catch (IOException e) {
            return "(unknown)";
        }
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
