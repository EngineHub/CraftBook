package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;


public class MoreRails extends AbstractCraftBookMechanic {

    public static MoreRails instance;

    @Override
    public boolean enable() {

        instance = this;
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (pressurePlate)
            if (event.getTo().getBlock().getType() == Material.STONE_PRESSURE_PLATE
                    || Tag.WOODEN_PRESSURE_PLATES.isTagged(event.getTo().getBlock().getType())
                    || event.getTo().getBlock().getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                    || event.getTo().getBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

        if (ladder) {
            double maxSpeed = ((Minecart) event.getVehicle()).getMaxSpeed();
            if (event.getTo().getBlock().getType() == Material.LADDER) {
                BlockFace face = ((Directional) event.getTo().getBlock().getBlockData()).getFacing().getOppositeFace();
                Vector velocity = new Vector(0, ladderVerticalVelocity, face.getModZ());
                if (velocity.length() > maxSpeed) {
                    double length = velocity.length() / maxSpeed;
                    velocity.setX(velocity.getX() / length);
                    velocity.setY(velocity.getY() / length);
                    velocity.setZ(velocity.getZ() / length);
                }
                velocity.add(new Vector(face.getModX(), 0, face.getModZ()));
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(velocity));
            } else if (event.getTo().getBlock().getType() == Material.VINE) {
                BlockFace movementFace = BlockFace.SELF;
                MultipleFacing vine = (MultipleFacing) event.getTo().getBlock().getBlockData();
                for (BlockFace test : vine.getAllowedFaces())
                    if (vine.hasFace(test)) {
                        movementFace = test;
                        break;
                    }
                if (movementFace == BlockFace.SELF)
                    return;
                Vector velocity = new Vector(0, ladderVerticalVelocity, 0);
                if (velocity.length() > maxSpeed) {
                    double length = velocity.length() / maxSpeed;
                    velocity.setX(velocity.getX() / length);
                    velocity.setY(velocity.getY() / length);
                    velocity.setZ(velocity.getZ() / length);
                }
                velocity.add(new Vector(movementFace.getModX(), 0, movementFace.getModZ()));
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(velocity));
            }
        }
    }

    public boolean ladder;
    private double ladderVerticalVelocity;
    public boolean pressurePlate;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "pressure-plate-intersection", "Enables the pressure plate as an intersection.");
        pressurePlate = config.getBoolean(path + "pressure-plate-intersection", false);

        config.setComment(path + "ladder-vertical-rail", "Enables the ladder as a vertical rail.");
        ladder = config.getBoolean(path + "ladder-vertical-rail", false);

        config.setComment(path + "ladder-vertical-rail-velocity", "Sets the velocity applied to the minecart on vertical rails.");
        ladderVerticalVelocity = config.getDouble(path + "ladder-vertical-rail-velocity", 0.5D);
    }
}