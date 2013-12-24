package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RailUtil;

public class VisionSteering extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {

        if(!event.getPlayer().isInsideVehicle())
            return;

        if(!(event.getPlayer().getVehicle() instanceof Minecart))
            return;

        if(Math.abs((double)event.getFrom().getYaw() - (double)event.getTo().getYaw()) < CraftBookPlugin.inst().getConfiguration().minecartVisionSteeringMinimumSensitivity)
            return;

        if(RailUtil.isTrack(event.getPlayer().getVehicle().getLocation().getBlock().getType()))
            return;

        Vector direction = event.getPlayer().getLocation().getDirection();
        direction = direction.normalize();
        direction.setY(0);
        direction = direction.multiply(event.getPlayer().getVehicle().getVelocity().length());
        direction.setY(event.getPlayer().getVehicle().getVelocity().getY());
        event.getPlayer().getVehicle().setVelocity(direction);
    }
}