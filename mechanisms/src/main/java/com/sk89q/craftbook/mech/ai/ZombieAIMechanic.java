package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class ZombieAIMechanic implements BaseAIMechanic {

    MechanismsPlugin plugin;

    public ZombieAIMechanic(MechanismsPlugin plugin) {

        this.plugin = plugin;
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {

        if (!(event.getEntity() instanceof Zombie) || event.getReason() == TargetReason.TARGET_ATTACKED_ENTITY)
            return; //Just making sure

        Zombie zombie = (Zombie) event.getEntity();
        if (event.getTarget() instanceof Player && !zombie.hasLineOfSight(event.getTarget())) // the target.
            if (!((Player) event.getTarget()).isSprinting()) {
                event.setCancelled(true);
                return;
            }
        if (zombie.getLocation().getBlock().getLightLevel() > 6) return; //They can clearly see the target.
        if (event.getTarget() instanceof Player) if (((Player) event.getTarget()).isSneaking() && event.getTarget().getLocation().distance(zombie
                .getLocation()) > 2) {
            event.setCancelled(true);
        }
    }
}