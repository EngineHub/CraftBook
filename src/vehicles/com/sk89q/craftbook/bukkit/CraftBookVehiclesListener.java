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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import com.sk89q.craftbook.VehiclesConfiguration;

public class CraftBookVehiclesListener extends VehicleListener {
    
    protected VehiclesPlugin plugin;
    
    public CraftBookVehiclesListener(VehiclesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when an vehicle moves.
     *
     * @param event
     */
    @Override
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        
        // Only working with minecarts
        if (!(vehicle instanceof Minecart)) {
            return;
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            handleMinecartBlockChange(event);
        }
    }
    
    protected void handleMinecartBlockChange(VehicleMoveEvent event) {
        Minecart minecart = (Minecart) event.getVehicle();
        Location to = event.getTo();
        
        Block under = to.getBlock().getRelative(0, -1, 0);
        int underType = under.getTypeId();
        
        VehiclesConfiguration config = plugin.getLocalConfiguration();

        if (underType == config.maxBoostBlock) {
            minecart.setVelocity(minecart.getVelocity().normalize().multiply(100));
        } else if (underType == config.slow20xBlock) {
            minecart.setVelocity(minecart.getVelocity().multiply(0.8));
        } else if (underType == config.slow50xBlock) {
            minecart.setVelocity(minecart.getVelocity().multiply(0.5));
        }
    }
}
