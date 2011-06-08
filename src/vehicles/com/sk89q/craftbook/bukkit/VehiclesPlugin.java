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

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.vehicle.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.cart.*;
import com.sk89q.worldedit.*;

/**
 * Plugin for CraftBook's redstone additions.
 * 
 * @author sk89q
 */
public class VehiclesPlugin extends BaseBukkitPlugin {
    
    private VehiclesConfiguration config;
    private VehicleListener lvehicle;
    private BlockListener lblock;
    private MinecartManager cartman;
    
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        cartman = new MinecartManager(this);
    }
    
    @Override
    protected void registerEvents() {
        createDefaultConfiguration("config.yml");
        
        // config has to be loaded before the listeners are built because they cache stuff
        config = new VehiclesConfiguration(getConfiguration(), getDataFolder());
        
        lvehicle = new CraftBookVehicleListener();
        lblock = new CraftBookVehicleBlockListener();
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_CREATE,  lvehicle, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE,    lvehicle, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, lblock,   Priority.Normal, this);
    }
    
    public VehiclesConfiguration getLocalConfiguration() {
        return config;
    }
    
    
    
    /**
     * Preprocesses event data coming directly from bukkit and passes it off to
     * appropriate logic in MinecartManager.
     */
    class CraftBookVehicleListener extends VehicleListener {
        public CraftBookVehicleListener() {}

        /**
         * Beware that turning this on makes it impossible for stations to
         * enforce their brake locking, and it also radically changes the impact
         * of mechanisms like boosters and brakes that multiply cart properties,
         * since it changes whether or not they are capable of multiplying their
         * own effects.
         */
        public static final boolean PROCESS_BOUNDARY_CROSS_ONLY = false;
        
        /**
         * Called when a vehicle is created.
         */
        @Override
        public void onVehicleCreate(VehicleCreateEvent event) {
            Vehicle vehicle = event.getVehicle();
            
            // Ignore events not relating to minecrarts.
            if (!(vehicle instanceof Minecart)) return;
            
            // Modify the vehicle properties according to config.
            VehiclesConfiguration config = getLocalConfiguration();
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
            
            if (PROCESS_BOUNDARY_CROSS_ONLY) {
                // Ignore events that don't involve crossing the boundary from one block to another.
                Location from = event.getFrom();
                Location to = event.getTo();
                if (from.getBlockX() == to.getBlockX()
                 && from.getBlockY() == to.getBlockY()
                 && from.getBlockZ() == to.getBlockZ()) return;
            }
            
            // ...Okay, go ahead then.
            cartman.impact(event);
        }
    }
    
    
    
    class CraftBookVehicleBlockListener extends BlockListener {
        public CraftBookVehicleBlockListener() {}
        
        @Override
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {
            // ignore events that are only changes in current strength
            if ((event.getOldCurrent() > 0) == (event.getNewCurrent() > 0)) return;
            
            // remember that bukkit only gives us redstone events for wires and things that already respond to redstone, which is entirely unhelpful.
            // So: issue four actual events per bukkit event.
            for (BlockFace bf : CartMechanism.powerSupplyOptions)
                cartman.impact(new SourcedBlockRedstoneEvent(event, event.getBlock().getFace(bf)));
        }
    }
}
