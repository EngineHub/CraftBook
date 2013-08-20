package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;

public class BoatUncrashable extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {

        if (!(event.getVehicle() instanceof Boat)) return;

        if (event.getAttacker() == null) {
            event.getVehicle().setVelocity(new Vector(0, 0, 0));
            event.setCancelled(true);
        }
    }
}