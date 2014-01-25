package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;

public class SpeedModifiers extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierMaxSpeed > 0)
            ((Boat) event.getVehicle()).setMaxSpeed(((Boat) event.getVehicle()).getMaxSpeed() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierMaxSpeed);
        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierUnnoccupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setUnoccupiedDeceleration(((Boat) event.getVehicle()).getUnoccupiedDeceleration() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierUnnoccupiedDeceleration);
        if(CraftBookPlugin.inst().getConfiguration().boatSpeedModifierOccupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setOccupiedDeceleration(((Boat) event.getVehicle()).getOccupiedDeceleration() * CraftBookPlugin.inst().getConfiguration().boatSpeedModifierOccupiedDeceleration);
    }
}