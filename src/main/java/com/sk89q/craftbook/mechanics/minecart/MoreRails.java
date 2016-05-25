package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Vine;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
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
            if (event.getTo().getBlock().getType() == Material.STONE_PLATE || event.getTo().getBlock().getType() == Material.WOOD_PLATE || event.getTo().getBlock().getType() == Material.GOLD_PLATE || event.getTo().getBlock().getType() == Material.IRON_PLATE)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

        if (ladder)
            if (event.getTo().getBlock().getType() == Material.LADDER) {
                Attachable ladder = (Attachable) event.getTo().getBlock().getState().getData();
                Vector velocity = new Vector(0,ladderVerticalVelocity,((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModZ());
                if(velocity.length() > ((Minecart) event.getVehicle()).getMaxSpeed()) {
                    double length = velocity.length()/((Minecart) event.getVehicle()).getMaxSpeed();
                    velocity.setX(velocity.getX() / length);
                    velocity.setY(velocity.getY() / length);
                    velocity.setZ(velocity.getZ() / length);
                }
                velocity.add(new Vector(ladder.getAttachedFace().getModX(), 0, ladder.getAttachedFace().getModZ()));
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(velocity));
            } else if (event.getTo().getBlock().getType() == Material.VINE) {
                BlockFace movementFace = BlockFace.SELF;
                Vine vine = (Vine) event.getTo().getBlock().getState().getData();
                for(BlockFace test : LocationUtil.getDirectFaces())
                    if(vine.isOnFace(test)) {
                        movementFace = test;
                        break;
                    }
                if(movementFace == BlockFace.SELF)
                    return;
                Vector velocity = new Vector(0,ladderVerticalVelocity,0);
                if(velocity.length() > ((Minecart) event.getVehicle()).getMaxSpeed()) {
                    double length = velocity.length()/((Minecart) event.getVehicle()).getMaxSpeed();
                    velocity.setX(velocity.getX() / length);
                    velocity.setY(velocity.getY() / length);
                    velocity.setZ(velocity.getZ() / length);
                }
                velocity.add(new Vector(movementFace.getModX(), 0, movementFace.getModZ()));
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(velocity));
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