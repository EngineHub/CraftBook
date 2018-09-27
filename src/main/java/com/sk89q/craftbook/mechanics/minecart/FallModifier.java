package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class FallModifier extends AbstractCraftBookMechanic {

    @Override
    public void disable() {
        fallSpeed = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        ((Minecart) event.getVehicle()).setFlyingVelocityMod(fallSpeed);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        ((Minecart) event.getVehicle()).setFlyingVelocityMod(fallSpeed);
    }

    private double verticalSpeed;
    private double horizontalSpeed;
    private Vector fallSpeed;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "vertical-fall-speed", "Sets the vertical fall speed of the minecart");
        verticalSpeed = config.getDouble(path + "vertical-fall-speed", 0.9D);

        config.setComment(path + "horizontal-fall-speed", "Sets the horizontal fall speed of the minecart");
        horizontalSpeed = config.getDouble(path + "horizontal-fall-speed", 1.1D);

        fallSpeed = new Vector(horizontalSpeed, verticalSpeed, horizontalSpeed);
    }
}