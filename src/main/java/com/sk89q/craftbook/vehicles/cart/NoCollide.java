package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class NoCollide extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCartCollision(VehicleEntityCollisionEvent event) {

        if (event.getVehicle() instanceof Minecart) {
            if (event.getVehicle().isEmpty() && !CraftBookPlugin.inst().getConfiguration().minecartNoCollideEmpty) return;
            if (!event.getVehicle().isEmpty() && !CraftBookPlugin.inst().getConfiguration().minecartNoCollideFull) return;

            event.setCollisionCancelled(true);
            return;
        }
    }
}