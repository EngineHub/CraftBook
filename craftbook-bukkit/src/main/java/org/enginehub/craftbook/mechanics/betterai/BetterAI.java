/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.betterai;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Particle;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BetterAI extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityCreate(EntityAddToWorldEvent event) {
        if (attackPassive.isEmpty() || !EventUtil.passesFilter(event)) {
            return;
        }

        if (isEntityEnabled(event.getEntity(), attackPassive) && event.getEntity() instanceof Monster monster) {
            Bukkit.getServer().getMobGoals().addGoal(monster, 5, new AttackPassiveGoal(monster, attackPassiveIgnoreHostileMounts));
        }

        if (isEntityEnabled(event.getEntity(), fleeFromWeapons) && event.getEntity() instanceof Creature creature) {
            if (Bukkit.getServer().getMobGoals().hasGoal(creature, VanillaGoal.PANIC)) {
                Bukkit.getServer().getMobGoals().addGoal(creature, 5, new FleeFromWeaponsGoal(creature));
            } else {
                CraftBookPlugin.inst().getLogger().warning("Attempted to add FleeFromWeaponsGoal to unsupported entity that does not have PANIC goal: " + event.getEntity().getType().key().asString());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySearchTarget(EntityTargetEvent event) {
        if (enhancedVision.isEmpty() || !EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getReason() != EntityTargetEvent.TargetReason.CLOSEST_PLAYER
            && event.getReason() != EntityTargetEvent.TargetReason.CLOSEST_ENTITY
            && event.getReason() != EntityTargetEvent.TargetReason.RANDOM_TARGET) {
            // Filter out to only these reasons.
            return;
        }

        if (isEntityEnabled(event.getEntity(), enhancedVision)) {
            if (event.getTarget() == null) {
                return;
            }

            Difficulty diff = event.getEntity().getWorld().getDifficulty();

            LivingEntity enemy = (LivingEntity) event.getEntity();
            if (event.getTarget() instanceof Player player && !enemy.hasLineOfSight(event.getTarget())) {
                if (!player.isSprinting()) {
                    CraftBookPlugin.logDebugMessage("Disabling entity target - Player is not visible.", "ai-mechanics.entity-target.vision");
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getTarget().getLocation().getBlock().getLightLevel() > (diff == Difficulty.HARD ? 4 : 6) && enemy.hasLineOfSight(event.getTarget())) {
                return; // They can clearly see the target.
            }
            if (event.getTarget() instanceof Player player) {
                if (player.isSneaking()) {
                    int distance = (int) Math.floor(player.getLocation().distanceSquared(enemy.getLocation()));
                    if (distance != 0 && ThreadLocalRandom.current().nextInt(distance) > (diff == Difficulty.HARD ? 4 : 2)) {
                        CraftBookPlugin.logDebugMessage("Disabling entity target - Player is sneaking.", "ai-mechanics.entity-target.vision");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (criticalBow.isEmpty() || !EventUtil.passesFilter(event)) {
            return;
        }

        if (isEntityEnabled(event.getEntity(), criticalBow)) {
            int amount = 0;
            switch (event.getEntity().getWorld().getDifficulty()) {
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

            if (ThreadLocalRandom.current().nextInt(amount) == 0) {
                CraftBookPlugin.logDebugMessage("Performing critical hit.", "ai-mechanics.shoot-bow.critical");
                event.getEntity().getWorld().spawnParticle(Particle.CRIT_MAGIC, event.getEntity().getEyeLocation(), 10);
                event.getProjectile().setFireTicks(5000);
            }
        }
    }

    private static boolean isEntityEnabled(Entity ent, Set<String> entities) {
        String id = BukkitAdapter.adapt(ent.getType()).id();
        if (id.startsWith("minecraft:") && entities.contains(id.substring("minecraft:".length()))) {
            // Special-case handling for removal of `minecraft:` namespace.
            return true;
        }
        return entities.contains(BukkitAdapter.adapt(ent.getType()).id());
    }

    private Set<String> enhancedVision;
    private Set<String> criticalBow;
    private Set<String> attackPassive;
    private Set<String> fleeFromWeapons;
    private boolean attackPassiveIgnoreHostileMounts;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enhanced-vision-enabled", "The list of entities to enable the enhanced vision AI mechanic for.");
        enhancedVision = Set.copyOf(config.getStringList("enhanced-vision-enabled", List.of(
            EntityTypes.ZOMBIE.id(), EntityTypes.DROWNED.id(), EntityTypes.HUSK.id(), EntityTypes.ZOMBIFIED_PIGLIN.id()
        )));

        config.setComment("critical-bow-enabled", "The list of entities to enable the critical bow AI mechanic for.");
        criticalBow = Set.copyOf(config.getStringList("critical-bow-enabled", List.of(
            EntityTypes.SKELETON.id()
        )));

        config.setComment("attack-passive-enabled", "The list of entities to enable the attack passive AI mechanic for.");
        attackPassive = Set.copyOf(config.getStringList("attack-passive-enabled", List.of(
            EntityTypes.ZOMBIE.id(), EntityTypes.DROWNED.id(), EntityTypes.HUSK.id()
        )));

        config.setComment("flee-from-weapons", "The list of entities to enable the flee from weapons AI mechanic for.");
        fleeFromWeapons = Set.copyOf(config.getStringList("flee-from-weapons", List.of(
            EntityTypes.CHICKEN.id(), EntityTypes.PIG.id(), EntityTypes.COW.id(), EntityTypes.MOOSHROOM.id(), EntityTypes.SHEEP.id()
        )));

        config.setComment("attack-passive-ignore-hostile-mounts", "Whether hostile mobs will ignore passive entities that are mounted by a hostile entity.");
        attackPassiveIgnoreHostileMounts = config.getBoolean("attack-passive-ignore-hostile-mounts", true);
    }
}
