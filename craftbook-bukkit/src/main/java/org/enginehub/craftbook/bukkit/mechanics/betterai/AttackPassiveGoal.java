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

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

class AttackPassiveGoal implements Goal<Monster> {
    private static final GoalKey<Monster> key = GoalKey.of(
        Monster.class,
        new NamespacedKey("craftbook", "attack_passive")
    );

    /**
     * Distance within which the target is considered active and will not be forgotten or overridden.
     */
    private final static int ACTIVE_DISTANCE = 15;
    /**
     * Distance within which to search for passive mobs to attack.
     */
    private final static double SEARCH_DISTANCE = 10d;
    /**
     * Distance beyond which the target will always be forgotten.
     */
    private final static int FORGET_DISTANCE = 60;

    private final Monster monster;
    private final boolean attackPassiveIgnoreHostileMounts;
    private @Nullable LivingEntity target;

    public AttackPassiveGoal(Monster monster, boolean attackPassiveIgnoreHostileMounts) {
        this.monster = monster;
        this.attackPassiveIgnoreHostileMounts = attackPassiveIgnoreHostileMounts;
    }

    @Override
    public boolean shouldActivate() {
        return !monster.isDead();
    }

    @Override
    public void start() {
        tick();
    }

    @Override
    public void stop() {
        this.target = null;
        monster.setTarget(null);
    }

    @Override
    public void tick() {
        if (target != null) {
            var targetValid = target.isValid() && !target.isDead() && target.getWorld().equals(monster.getWorld());
            var distanceSquared = target.getLocation().distanceSquared(monster.getLocation());

            if (targetValid && distanceSquared < ACTIVE_DISTANCE * ACTIVE_DISTANCE) {
                // Not outside of active distance, keep target & skip the search.
                return;
            }

            if (!targetValid || distanceSquared > FORGET_DISTANCE * FORGET_DISTANCE) {
                // Not a valid target, or beyond the forget distance - remove it and allow another search
                this.target = null;
                monster.setTarget(null);
            }

            // If it's valid & inside forget distance, but outside active distance, we continue to search for a closer target.
        }

        Animals closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity ent : monster.getNearbyEntities(SEARCH_DISTANCE, SEARCH_DISTANCE, SEARCH_DISTANCE)) {
            if (ent instanceof Animals animal && monster.hasLineOfSight(animal)) {
                if (attackPassiveIgnoreHostileMounts && !animal.getPassengers().isEmpty()) {
                    boolean foundAny = false;
                    for (Entity passenger : animal.getPassengers()) {
                        if (passenger instanceof Monster) {
                            foundAny = true;
                            break;
                        }
                    }
                    if (foundAny) {
                        continue;
                    }
                }
                double dist = animal.getLocation().distanceSquared(monster.getLocation());
                if (dist < closestDist) {
                    closest = animal;
                    closestDist = dist;
                }
            }
        }

        if (closest != null) {
            target = closest;
            monster.setTarget(this.target);
            CraftBookPlugin.logDebugMessage("Setting target to entity: " + closest.getType().name(), "ai-mechanics.entity-target.attack-passive");
        }
    }

    @Override
    public GoalKey<Monster> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }
}
