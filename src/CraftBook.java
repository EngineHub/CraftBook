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

/**
 * Entry point for the plugin for hey0's mod.
 *
 * @author sk89q
 */
public class CraftBook extends Plugin {
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
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        properties.load();

        listener.useBookshelf = properties.getBoolean("bookshelf-enable", true);
        listener.useLightSwitch = properties.getBoolean("light-switch-enable", true);
        listener.useGate = properties.getBoolean("gate-enable", true);
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
    }
}
