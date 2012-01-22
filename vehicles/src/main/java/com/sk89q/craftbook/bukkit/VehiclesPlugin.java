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

import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.cart.CartMechanism;
import com.sk89q.craftbook.cart.MinecartManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

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
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_EXIT,  lvehicle, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_DESTROY,  lvehicle, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY,  lvehicle, Priority.Normal, this);
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
         * Called when a vehicle hits an entity 
         */
        @Override
        public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
            VehiclesConfiguration config = getLocalConfiguration();
            Vehicle vehicle = event.getVehicle();
            Entity entity = event.getEntity();
            if (entity instanceof Player) return;
            if (!config.boatRemoveEntities && !config.minecartRemoveEntities) return;
            if (config.boatRemoveEntities ==  true && (vehicle instanceof Boat)) {
                event.getEntity().remove();
            }
            if (config.minecartRemoveEntities ==  true && (vehicle instanceof Minecart)) {
                entity.remove();
            }
        }
            
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
         * Called when a vehicle is exited
         */
        
        @Override
        public void onVehicleExit(VehicleExitEvent event) {
            Vehicle vehicle = event.getVehicle();
            
            if (!(vehicle instanceof Minecart)) return;
            
            VehiclesConfiguration config = getLocalConfiguration();
            if (config.minecartRemoveOnExit) {
                vehicle.remove();
            }
        }
        /**
         * Called when an vehicle moves.
         */
        @Override
        public void onVehicleMove(VehicleMoveEvent event) {
            // Ignore events not relating to minecarts.
            if (!(event.getVehicle() instanceof Minecart)) return;
            
            cartman.impact(event);
        }
        /**
         * Called when a vehicle is destroied.
         */
        @Override
        public void onVehicleDestroy(VehicleDestroyEvent event) {
            if (!(event.getVehicle() instanceof Boat)) return;
            
            VehiclesConfiguration config = getLocalConfiguration();
            if (config.boatBreakReturn == true) {
                ItemStack boatStack = new ItemStack(Material.BOAT, 1);
                Boat boat =  (Boat) event.getVehicle();
                Location loc = boat.getLocation();
                loc.getWorld().dropItemNaturally(loc, boatStack);
                boat.remove();
                event.setCancelled(true);
            } else {
                return;
            }
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
