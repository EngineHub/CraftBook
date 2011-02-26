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

import org.bukkit.util.config.Configuration;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.mech.*;

/**
 * Plugin for CraftBook's mechanisms.
 * 
 * @author sk89q
 */
public class MechanismsPlugin extends BaseBukkitPlugin {
    
    protected MechanismsConfiguration config;
    
    @Override
    public void onEnable() {
        super.onEnable();

        createDefaultConfiguration("books.txt");
        createDefaultConfiguration("cauldron-recipes.txt");
        
        config = new MechanismsConfiguration() {
            @Override
            public void loadConfiguration() {
                Configuration config = getConfiguration();
                
                dataFolder = getDataFolder();
                bookcaseReadLine = config.getString("bookcase.read-line", bookcaseReadLine);
                bridgeSettings = new Bridge.BridgeSettings(true);
            }
        };
        
        config.loadConfiguration();
        
        MechanicManager manager = new MechanicManager();
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);
        
        // Let's register mechanics!
        manager.register(new BookcaseFactory(this));
        manager.register(new GateFactory(this));
        manager.register(new Bridge.BridgeFactory(this));
    }
    
    @Override
    protected void registerEvents() {
    }
    
    public MechanismsConfiguration getLocalConfiguration() {
        return config;
    }
}
