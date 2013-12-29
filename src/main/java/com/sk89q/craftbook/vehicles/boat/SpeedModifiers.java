package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class SpeedModifiers extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if (!(event.getVehicle() instanceof Boat)) return;

        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierMaxSpeed > 0)
            ((Boat) event.getVehicle()).setMaxSpeed(((Boat) event.getVehicle()).getMaxSpeed() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierMaxSpeed);
        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierUnnoccupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setUnoccupiedDeceleration(((Boat) event.getVehicle()).getUnoccupiedDeceleration() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierUnnoccupiedDeceleration);
        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierOccupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setOccupiedDeceleration(((Boat) event.getVehicle()).getOccupiedDeceleration() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierOccupiedDeceleration);
    }
}