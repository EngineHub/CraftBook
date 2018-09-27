package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class VisionSteering extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getPlayer().isInsideVehicle())
            return;

        if(!(event.getPlayer().getVehicle() instanceof Minecart))
            return;

        if(Math.abs((double)event.getFrom().getYaw() - (double)event.getTo().getYaw()) < minimumSensitivity)
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

    private int minimumSensitivity;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "minimum-sensitivity", "Sets the sensitivity of Vision Steering.");
        minimumSensitivity = config.getInt(path + "minimum-sensitivity", 3);
    }
}