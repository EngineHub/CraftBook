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

import com.sk89q.craftbook.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

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
     * Properties files for CraftBook.
     */
    private PropertiesFile properties = new PropertiesFile("craftbook.properties");
    
    /**
     * Listener for the plugin system.
     */
    private static final CraftBookListener listener =
            new CraftBookListener();

    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        PluginLoader loader = etc.getLoader();

        loader.addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this,
                PluginListener.Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this,
                PluginListener.Priority.MEDIUM);
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        properties.load();

        listener.books = properties.getBoolean("bookshelf-enable", true)
            ? new BookReader()
            : null;
        listener.lightSwitch = properties.getBoolean("light-switch-enable", true)
            ? new LightSwitch()
            : null;
        listener.gateSwitch = properties.getBoolean("gate-enable", true)
            ? new GateSwitch()
            : null;
        listener.elevator = properties.getBoolean("elevators-enable", true)
            ? new Elevator()
            : null;
        listener.dropBookshelves = properties.getBoolean("drop-bookshelves", true);
        listener.dropAppleChance = (float)(properties.getInt("apple-drop-chance", 5) / 100.0);
        listener.cauldron = null;

        if (properties.getBoolean("cauldron-enable", true)) {
            try {
                CauldronCookbook recipes =
                        CauldronCookbook.readCauldronRecipes("cauldron-recipes.txt");

                if (recipes.size() != 0) {
                    listener.cauldron = new Cauldron(recipes);
                    logger.log(Level.INFO, recipes.size() + " cauldron recipes loaded");
                } else {
                    logger.log(Level.WARNING, "cauldron-recipes.txt had no recipes");
                }
            } catch (IOException e) {
                logger.log(Level.INFO, "cauldron-recipes.txt not loaded: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
    }

    protected static int getBlockID(int x, int y, int z) {
        return etc.getServer().getBlockIdAt(x, y, z);
    }

    protected static int getBlockID(Vector pt) {
        return etc.getServer().getBlockIdAt(pt.getBlockX(),
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
}
