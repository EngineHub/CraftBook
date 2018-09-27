package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class CollisionEntry extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.LOW)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getVehicle() instanceof RideableMinecart) {
            if (!event.getVehicle().isEmpty()) return;
            if (!(event.getEntity() instanceof HumanEntity)) return;
            event.getVehicle().addPassenger(event.getEntity());

            event.setCollisionCancelled(true);
        }
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}