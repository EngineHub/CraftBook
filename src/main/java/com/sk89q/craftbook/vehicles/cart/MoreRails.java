package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Vine;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class MoreRails implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsPressurePlate)
            if (event.getTo().getBlock().getTypeId() == BlockID.STONE_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.WOODEN_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.PRESSURE_PLATE_HEAVY || event.getTo().getBlock().getTypeId() == BlockID.PRESSURE_PLATE_LIGHT)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadder)
            if (event.getTo().getBlock().getTypeId() == BlockID.LADDER)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(new Vector(((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModX(),CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadderVelocity,((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModZ())));
            else if (event.getTo().getBlock().getTypeId() == BlockID.VINE) {
                BlockFace movementFace = BlockFace.SELF;
                Vine vine = (Vine) event.getTo().getBlock().getState().getData();
                for(BlockFace test : LocationUtil.getDirectFaces())
                    if(vine.isOnFace(test)) {
                        movementFace = test;
                        break;
                    }
                if(movementFace == BlockFace.SELF)
                    return;
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(new Vector(movementFace.getModX(),CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadderVelocity,movementFace.getModZ())));
            }
    }
}