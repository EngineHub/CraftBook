package com.sk89q.craftbook.mechanics.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class SpeedModifiers extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        if(maxSpeed > 0)
            ((Boat) event.getVehicle()).setMaxSpeed(((Boat) event.getVehicle()).getMaxSpeed() * maxSpeed);
        if(unnoccupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setUnoccupiedDeceleration(((Boat) event.getVehicle()).getUnoccupiedDeceleration() * unnoccupiedDeceleration);
        if(occupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setOccupiedDeceleration(((Boat) event.getVehicle()).getOccupiedDeceleration() * occupiedDeceleration);
    }

    private double maxSpeed;
    private double unnoccupiedDeceleration;
    private double occupiedDeceleration;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "max-speed", "Sets the maximum speed of a boat. 0.4D is normal maximum speed.");
        maxSpeed = config.getDouble(path + "max-speed", 0.4D);

        config.setComment(path + "unnoccupied-deceleration", "Sets the unnoccupied deceleration of a boat. -1 is disabled.");
        unnoccupiedDeceleration = config.getDouble(path + "unnoccupied-deceleration", -1);

        config.setComment(path + "occupied-deceleration", "Sets the occupied deceleration of a boat. 0.3 is normal occupied deceleration");
        occupiedDeceleration = config.getDouble(path + "occupied-deceleration", 0.2);
    }
}