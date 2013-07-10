package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

import com.sk89q.craftbook.util.EntityUtil;

public class ExitRemover implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {

        Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof Minecart)) return;

        EntityUtil.killEntity(vehicle);
    }
}