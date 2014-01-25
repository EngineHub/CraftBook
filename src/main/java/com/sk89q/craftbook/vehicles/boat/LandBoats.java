package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;

public class LandBoats extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        ((Boat) event.getVehicle()).setWorkOnLand(true);
    }
}