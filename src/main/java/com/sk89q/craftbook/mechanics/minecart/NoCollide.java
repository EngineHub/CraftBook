package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;

public class NoCollide extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onCartCollision(VehicleEntityCollisionEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getVehicle() instanceof Minecart) {
            if (event.getVehicle().isEmpty() && !CraftBookPlugin.inst().getConfiguration().minecartNoCollideEmpty) return;
            if (!event.getVehicle().isEmpty() && !CraftBookPlugin.inst().getConfiguration().minecartNoCollideFull) return;

            event.setCollisionCancelled(true);
            return;
        }
    }
}