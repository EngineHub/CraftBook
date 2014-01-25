package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;

public class MobBlocker extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleEnter(VehicleEnterEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getVehicle().getWorld().isChunkLoaded(event.getVehicle().getLocation().getBlockX() >> 4, event.getVehicle().getLocation().getBlockZ() >> 4))
            return;

        Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof Minecart)) return;

        if(!(event.getEntered() instanceof Player))
            event.setCancelled(true);
    }
}