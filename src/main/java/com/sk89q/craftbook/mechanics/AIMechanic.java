package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Difficulty;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AIMechanic extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {

        if (event.getEntity() == null || event.getEntity().getType() == null || event.getEntity().getType().getName() == null) return;

        if (!EventUtil.passesFilter(event)) return;

        if(isEntityEnabled(event.getEntity(), attackPassiveEnabled)) {
            if(event.getTarget() != null) return;
            if(!(event.getEntity() instanceof LivingEntity)) return;
            for(Entity ent : event.getEntity().getNearbyEntities(15D, 15D, 15D)) {
                if(ent instanceof Animals && ((LivingEntity) event.getEntity()).hasLineOfSight(ent)) {
                    if(event.getEntity() instanceof Monster) {
                        event.setCancelled(true);
                        ((Monster) event.getEntity()).setTarget((Animals) ent);
                    } else
                        event.setTarget(ent);
                    CraftBookPlugin.logDebugMessage("Setting target to entity: " + ent.getType().name(), "ai-mechanics.entity-target.attack-passive");
                    return;
                }
            }
        }

        if(isEntityEnabled(event.getEntity(), visionEnabled)) {
            if(event.getTarget() == null) return;
            Difficulty diff = event.getEntity().getWorld().getDifficulty();

            if (event.getReason() != TargetReason.CLOSEST_PLAYER && event.getReason() != TargetReason.RANDOM_TARGET)
                return; // Just making sure

            LivingEntity enemy = (LivingEntity) event.getEntity();
            if (event.getTarget() instanceof Player && !enemy.hasLineOfSight(event.getTarget())) // the target.
                if (!((Player) event.getTarget()).isSprinting()) {
                    CraftBookPlugin.logDebugMessage("Disabling entity target - Player is not visible.", "ai-mechanics.entity-target.vision");
                    event.setCancelled(true);
                    return;
                }
            if (event.getTarget().getLocation().getBlock().getLightLevel() > (diff == Difficulty.HARD ? 4 : 6) && enemy.hasLineOfSight(event.getTarget())) return; // They can clearly see the target.
            if (event.getTarget() instanceof Player)
                if (((Player) event.getTarget()).isSneaking()) {
                    int distance = (int) Math.floor(event.getTarget().getLocation().distanceSquared(enemy.getLocation()));
                    if (distance != 0 && CraftBookPlugin.inst().getRandom().nextInt(distance) > (diff == Difficulty.HARD ? 4 : 2)) {
                        CraftBookPlugin.logDebugMessage("Disabling entity target - Player is sneaking.", "ai-mechanics.entity-target.vision");
                        event.setCancelled(true);
                    }
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityShootBow(EntityShootBowEvent event) {

        if (event.getEntity() == null || event.getEntity().getType() == null || event.getEntity().getType().getName() == null) return;

        if (!EventUtil.passesFilter(event)) return;

        if(isEntityEnabled(event.getEntity(), critBowEnabled)) {
            int amount = 0;
            switch(event.getEntity().getWorld().getDifficulty()) {
                case EASY:
                    amount = 100;
                    break;
                case HARD:
                    amount = 20;
                    break;
                case NORMAL:
                    amount = 50;
                    break;
                case PEACEFUL:
                    return;
            }
            if (CraftBookPlugin.inst().getRandom().nextInt(amount) == 0) {
                CraftBookPlugin.logDebugMessage("Performing critical hit.", "ai-mechanics.shoot-bow.critical");
                event.getProjectile().setFireTicks(5000);
            }
        }
    }

    private static boolean isEntityEnabled(Entity ent, List<String> entities) {

        if(entities == null) return false;
        for(String entity : entities)
            if(entity != null)
                if(ent.getType().getName().equalsIgnoreCase(entity) || ent.getType().name().equalsIgnoreCase(entity))
                    return true;
        return false;
    }

    @Override
    public boolean enable () {
        return visionEnabled.size() > 0 && attackPassiveEnabled.size() > 0 && critBowEnabled.size() > 0;
    }

    private List<String> visionEnabled;
    private List<String> critBowEnabled;
    private List<String> attackPassiveEnabled;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "vision-enable", "The list of entities to enable vision AI mechanics for.");
        visionEnabled = config.getStringList(path + "vision-enable", Arrays.asList("Zombie","PigZombie"));

        config.setComment(path + "crit-bow-enable", "The list of entities to enable bow critical AI mechanics for.");
        critBowEnabled = config.getStringList(path + "crit-bow-enable", Collections.singletonList("Skeleton"));

        config.setComment(path + "attack-passive-enable", "The list of entities to enable attack passive AI mechanics for.");
        attackPassiveEnabled = config.getStringList(path + "attack-passive-enable", Collections.singletonList("Zombie"));
    }
}