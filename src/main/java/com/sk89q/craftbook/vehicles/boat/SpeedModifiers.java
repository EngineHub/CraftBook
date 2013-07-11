package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class SpeedModifiers implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if (!(event.getVehicle() instanceof Boat)) return;

        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierMaxSpeed > 0)
            ((Boat) event.getVehicle()).setMaxSpeed(((Boat) event.getVehicle()).getMaxSpeed() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierMaxSpeed);
    }
}