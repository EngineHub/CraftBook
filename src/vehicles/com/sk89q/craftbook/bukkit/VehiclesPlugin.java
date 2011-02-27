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

import org.bukkit.event.Event;
import org.bukkit.util.config.Configuration;
import com.sk89q.craftbook.VehiclesConfiguration;

/**
 * Plugin for CraftBook's redstone additions.
 * 
 * @author sk89q
 */
public class VehiclesPlugin extends BaseBukkitPlugin {
    
    protected VehiclesConfiguration config;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        createDefaultConfiguration("config.yml");
        
        final VehiclesPlugin plugin = this;
        
        config = new VehiclesConfiguration() {
            @Override
            public void loadConfiguration() {
                Configuration config = plugin.getConfiguration();

                maxBoostBlock = config.getInt("max-boost-block", maxBoostBlock);
                slow50xBlock = config.getInt("50x-slow-block", slow50xBlock);
                slow20xBlock = config.getInt("20x-slow-block", slow20xBlock);
            }
        };
        
        config.loadConfiguration();
    }
    
    @Override
    protected void registerEvents() {
        CraftBookVehiclesListener vehiclesListener = new CraftBookVehiclesListener(this);
        
        registerEvent(Event.Type.VEHICLE_MOVE, vehiclesListener);
    }
    
    public VehiclesConfiguration getLocalConfiguration() {
        return config;
    }
}
