package com.sk89q.craftbook.vehicles.cart;

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
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;


public class MoreRails extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsPressurePlate)
            if (event.getTo().getBlock().getType() == Material.STONE_PLATE || event.getTo().getBlock().getType() == Material.WOOD_PLATE || event.getTo().getBlock().getType() == Material.GOLD_PLATE || event.getTo().getBlock().getType() == Material.IRON_PLATE)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadder)
            if (event.getTo().getBlock().getType() == Material.LADDER) {
                Attachable ladder = (Attachable) event.getTo().getBlock().getState().getData();
                Vector velocity = new Vector(0,CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadderVelocity,((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModZ());
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
                Vector velocity = new Vector(0,CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadderVelocity,0);
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
}