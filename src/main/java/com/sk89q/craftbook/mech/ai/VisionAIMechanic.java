package com.sk89q.craftbook.mech.ai;

import org.bukkit.Difficulty;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class VisionAIMechanic extends BaseAIMechanic implements TargetAIMechanic {

    public VisionAIMechanic(EntityType ... entity) {

        super(entity);
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {

        if(event.getTarget() == null) return;
        Difficulty diff = event.getEntity().getWorld().getDifficulty();

        if (event.getReason() != TargetReason.CLOSEST_PLAYER && event.getReason() != TargetReason.RANDOM_TARGET)
            return; // Just making sure

        LivingEntity enemy = (LivingEntity) event.getEntity();
        if (event.getTarget() instanceof Player && !enemy.hasLineOfSight(event.getTarget())) // the target.
            if (!((Player) event.getTarget()).isSprinting()) {
                event.setCancelled(true);
                return;
            }
        if (event.getTarget().getLocation().getBlock().getLightLevel() > (diff == Difficulty.HARD ? 4 : 6) && enemy.hasLineOfSight(event.getTarget())) return; // They can clearly see the target.
        if (event.getTarget() instanceof Player)
            if (((Player) event.getTarget()).isSneaking()) {
                int distance = (int) Math.floor(event.getTarget().getLocation().distanceSquared(enemy.getLocation()));
                if (distance != 0 && CraftBookPlugin.inst().getRandom().nextInt(distance) > (diff == Difficulty.HARD ? 4 : 2))
                    event.setCancelled(true);
            }
    }
}