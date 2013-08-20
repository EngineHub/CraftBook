package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RailUtil;

public class ConstantSpeed extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (RailUtil.isTrack(event.getTo().getBlock().getTypeId()) && event.getVehicle().getVelocity().lengthSquared() > 0) {
            Vector vel = event.getVehicle().getVelocity();
            event.getVehicle().setVelocity(vel.normalize().multiply(CraftBookPlugin.inst().getConfiguration().minecartConstantSpeedSpeed));
        }
    }
}