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
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class FleeFromWeaponsGoal implements Goal<Creature> {
    private static final GoalKey<Creature> key = GoalKey.of(
        Creature.class,
        new NamespacedKey("craftbook", "flee_from_weapons")
    );

    private final Creature creature;

    public FleeFromWeaponsGoal(Creature creature) {
        this.creature = creature;
    }

    private boolean isNearbyPlayerWithWeapon() {
        var entities = this.creature.getNearbyEntities(5, 5, 5);
        for (Entity entity : entities) {
            if (entity instanceof Player player) {
                Material mainHandType = player.getInventory().getItemInMainHand().getType();
                Material offHandType = player.getInventory().getItemInOffHand().getType();

                if (Tag.ITEMS_SWORDS.isTagged(mainHandType) || Tag.ITEMS_SWORDS.isTagged(offHandType)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldActivate() {
        return !creature.isDead() && isNearbyPlayerWithWeapon();
    }

    @Override
    public void start() {
        if (isNearbyPlayerWithWeapon()) {
            Goal<Creature> panicGoal = Bukkit.getServer().getMobGoals().getGoal(this.creature, VanillaGoal.PANIC);
            if (panicGoal != null) {
                panicGoal.start();
            }
        }
    }

    @Override
    public void tick() {
        if (isNearbyPlayerWithWeapon()) {
            Goal<Creature> panicGoal = Bukkit.getServer().getMobGoals().getGoal(this.creature, VanillaGoal.PANIC);
            if (panicGoal != null) {
                panicGoal.start();
            }
        }
    }

    @Override
    public GoalKey<Creature> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
