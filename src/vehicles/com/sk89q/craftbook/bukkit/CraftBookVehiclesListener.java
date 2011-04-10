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
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.cart.*;

/**
 * Preprocesses event data coming directly from bukkit and passes it off to
 * appropriate logic in MinecartManager.
 */
public class CraftBookVehiclesListener extends VehicleListener { 
    public CraftBookVehiclesListener(VehiclesPlugin plugin) {
        this.plugin = plugin;
        this.cartman = new MinecartManager(plugin);
    }
    
    protected VehiclesPlugin plugin;
    protected MinecartManager cartman;
    
    /**
     * Called when a vehicle is created.
     */
    @Override
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        
        // Ignore events not relating to minecrarts.
        if (!(vehicle instanceof Minecart)) return;
        
        // Modify the vehicle properties according to config.
        VehiclesConfiguration config = plugin.getLocalConfiguration();
        Minecart minecart = (Minecart) vehicle;
        minecart.setSlowWhenEmpty(config.minecartSlowWhenEmpty);
        minecart.setMaxSpeed(minecart.getMaxSpeed() * config.minecartMaxSpeedModifier);
    }
    
    /**
     * Called when an vehicle moves.
     */
    @Override
    public void onVehicleMove(VehicleMoveEvent event) {
        // Ignore events not relating to minecrarts.
        if (!(event.getVehicle() instanceof Minecart)) return;
        
        // Ignore events that don't involve crossing the boundary from one block to another.
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX()
         && from.getBlockY() == to.getBlockY()
         && from.getBlockZ() == to.getBlockZ()) return;
        
        // ...Okay, go ahead then.
        cartman.handleMinecartBlockChange(event);
    }
}
