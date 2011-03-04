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

import com.circuits.com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.circuits.JackOLanternFactory;
import com.sk89q.craftbook.circuits.NetherrackFactory;

/**
 * Plugin for CraftBook's redstone additions.
 * 
 * @author sk89q
 */
public class CircuitsPlugin extends BaseBukkitPlugin {
    
    protected CircuitsConfiguration config;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        createDefaultConfiguration("custom-ics.txt");
        
        config = new CircuitsConfiguration() {
            @Override
            public void loadConfiguration() {
            }
        };
        
        config.loadConfiguration();
        
        MechanicManager manager = new MechanicManager();
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);
        
        // Let's register mechanics!
        manager.register(new NetherrackFactory());
        manager.register(new JackOLanternFactory());
    }
    
    @Override
    protected void registerEvents() {
    }
    
    public CircuitsConfiguration getLocalConfiguration() {
        return config;
    }
}
