// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.craftbook.LocalPlayer;

/**
 * Base plugin class for CraftBook for child CraftBook plugins.
 * 
 * @author sk89q
 */
public abstract class BaseBukkitPlugin extends JavaPlugin {
    
    /**
     * Logger for messages.
     */
    protected static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    /**
     * Called when the plugin is enabled. This is where configuration is loaded,
     * and the plugin is setup.
     */
    public void onEnable() {
        logger.info(getDescription().getName() + " "
                + getDescription().getVersion() + " enabled.");
        
        // Make the data folder for the plugin where configuration files
        // and other data files will be stored
        getDataFolder().mkdirs();
        
        // Register events
        registerEvents();
    }

    /**
     * Called when the plugin is disabled. Shutdown and clearing of any
     * temporary data occurs here.
     */
    public void onDisable() {
    }
    
    /**
     * Register the events that are used.
     */
    protected abstract void registerEvents();
    
    /**
     * Register an event.
     * 
     * @param type
     * @param listener
     * @param priority
     */
    protected void registerEvent(Event.Type type, Listener listener, Priority priority) {
        getServer().getPluginManager()
                .registerEvent(type, listener, priority, this);
    }
    
    /**
     * Register an event at normal priority.
     * 
     * @param type
     * @param listener
     */
    protected void registerEvent(Event.Type type, Listener listener) {
        getServer().getPluginManager()
                .registerEvent(type, listener, Priority.Normal, this);
    }
    
    /**
     * Create a default configuration file from the .jar.
     * 
     * @param name
     */
    protected void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            
            InputStream input =
                    this.getClass().getResourceAsStream("/defaults/" + name);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    
                    logger.info(getDescription().getName()
                            + ": Default configuration file written: " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (IOException e) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {}
                }
            }
        }
    }
    
    /**
     * Get a player.
     * 
     * @param player Bukkit Player object
     * @return
     */
    public LocalPlayer wrap(Player player) {
        return new BukkitPlayer(player);
    }
}
