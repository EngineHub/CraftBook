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

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

class AttackPassiveGoal implements Goal<Monster> {
    private static final GoalKey<Monster> key = GoalKey.of(
        Monster.class,
        new NamespacedKey("craftbook", "attack_passive")
    );

    private final Monster monster;
    private final boolean attackPassiveIgnoreHostileMounts;
    private LivingEntity target;

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
        if (target != null && !target.isDead()) {
            if (target.getWorld().equals(monster.getWorld())
                && target.getLocation().distanceSquared(monster.getLocation()) < 15*15) {
                return;
            }
        }

        Animals closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity ent : monster.getNearbyEntities(10D, 10D, 10D)) {
            if (ent instanceof Animals && monster.hasLineOfSight(ent)) {
                if (attackPassiveIgnoreHostileMounts && !ent.getPassengers().isEmpty()) {
                    boolean foundAny = false;
                    for (Entity passenger : ent.getPassengers()) {
                        if (passenger instanceof Monster) {
                            foundAny = true;
                            break;
                        }
                    }
                    if (foundAny) {
                        continue;
                    }
                }
                double dist = ent.getLocation().distanceSquared(monster.getLocation());
                if (dist < closestDist) {
                    closest = (Animals) ent;
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
    public @NotNull GoalKey<Monster> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }
}
