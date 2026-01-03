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

package org.enginehub.craftbook.bukkit.mechanics.betterai;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.betterai.BetterAI;
import org.enginehub.craftbook.util.EventUtil;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BukkitBetterAI extends BetterAI implements Listener {

    private static final NamespacedKey SIZE_VARIANCE = new NamespacedKey("craftbook", "size_variance");
    private static final NamespacedKey SIZE_VARIANCE_BREEDING = new NamespacedKey("craftbook", "size_variance_breeding");

    public BukkitBetterAI(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityCreate(EntityAddToWorldEvent event) {
        if (areAllDisabled(attackPassive, sizeVariance, fleeFromWeapons) || !EventUtil.passesFilter(event)) {
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

        if (isEntityEnabled(event.getEntity(), sizeVariance) && event.getEntity() instanceof LivingEntity livingEntity) {
            CreatureSpawnEvent.SpawnReason spawnReason = livingEntity.getEntitySpawnReason();
            if (spawnReason != CreatureSpawnEvent.SpawnReason.BREEDING && spawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM && spawnReason != CreatureSpawnEvent.SpawnReason.COMMAND) {
                AttributeInstance attributeInstance = livingEntity.getAttribute(Attribute.SCALE);
                if (attributeInstance.getModifier(SIZE_VARIANCE) == null && attributeInstance.getModifier(SIZE_VARIANCE_BREEDING) == null) {
                    attributeInstance.addModifier(
                        new AttributeModifier(SIZE_VARIANCE, (Math.random() - 0.5) * 2 * sizeVarianceVariability, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY)
                    );
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityBreed(EntityBreedEvent event) {
        if (areAllDisabled(sizeVariance) || !EventUtil.passesFilter(event)) {
            return;
        }

        if (sizeVarianceAllowBreeding && isEntityEnabled(event.getEntity(), sizeVariance)) {
            double averageSize = (event.getMother().getAttribute(Attribute.SCALE).getValue() + event.getFather().getAttribute(Attribute.SCALE).getValue()) / 2.0;
            AttributeInstance attributeInstance = event.getEntity().getAttribute(Attribute.SCALE);
            attributeInstance.setBaseValue(averageSize);
            if (attributeInstance.getModifier(SIZE_VARIANCE_BREEDING) == null) {
                attributeInstance.addModifier(
                    new AttributeModifier(SIZE_VARIANCE_BREEDING, (Math.random() - 0.5) * 2 * sizeVarianceBreedingVariability, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY)
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySearchTarget(EntityTargetEvent event) {
        if (areAllDisabled(enhancedVision) || !EventUtil.passesFilter(event)) {
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
        if (areAllDisabled(criticalBow) || !EventUtil.passesFilter(event)) {
            return;
        }

        if (isEntityEnabled(event.getEntity(), criticalBow)) {
            int amount = switch (event.getEntity().getWorld().getDifficulty()) {
                case EASY -> 100;
                case HARD -> 20;
                case NORMAL -> 50;
                case PEACEFUL -> 0;
            };

            if (amount > 0 && ThreadLocalRandom.current().nextInt(amount) == 0) {
                CraftBookPlugin.logDebugMessage("Performing critical hit.", "ai-mechanics.shoot-bow.critical");
                event.getEntity().getWorld().spawnParticle(Particle.CRIT, event.getEntity().getEyeLocation(), 10);
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
}
