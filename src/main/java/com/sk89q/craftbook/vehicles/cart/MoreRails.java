package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Attachable;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;

public class MoreRails implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsPressurePlate)
            if (event.getTo().getBlock().getTypeId() == BlockID.STONE_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.WOODEN_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.PRESSURE_PLATE_HEAVY || event.getTo().getBlock().getTypeId() == BlockID.PRESSURE_PLATE_LIGHT)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadder)
            if (event.getTo().getBlock().getTypeId() == BlockID.LADDER || event.getTo().getBlock().getTypeId() == BlockID.VINE)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(new Vector(((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModX(),0.5,((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModY())));
    }
}