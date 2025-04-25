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

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.util.List;
import java.util.Set;

public abstract class BetterAI extends AbstractCraftBookMechanic {

    public BetterAI(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    /**
     * Minor convenience method to make it easier to check if logic used for these mechanics can be skipped.
     *
     * @param entitySets The entity sets to check.
     * @return True if all of the sets are empty.
     */
    protected static boolean areAllDisabled(Set<?>... entitySets) {
        for (Set<?> entitySet : entitySets) {
            if (!entitySet.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    protected Set<String> enhancedVision;
    protected Set<String> criticalBow;
    protected Set<String> attackPassive;
    protected boolean attackPassiveIgnoreHostileMounts;
    protected Set<String> fleeFromWeapons;
    protected Set<String> sizeVariance;
    protected boolean sizeVarianceAllowBreeding;
    protected double sizeVarianceVariability;
    protected double sizeVarianceBreedingVariability;

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

        config.setComment("attack-passive-ignore-hostile-mounts", "Whether hostile mobs will ignore passive entities that are mounted by a hostile entity.");
        attackPassiveIgnoreHostileMounts = config.getBoolean("attack-passive-ignore-hostile-mounts", true);

        config.setComment("flee-from-weapons", "The list of entities to enable the flee from weapons AI mechanic for.");
        fleeFromWeapons = Set.copyOf(config.getStringList("flee-from-weapons", List.of(
            EntityTypes.CHICKEN.id(), EntityTypes.PIG.id(), EntityTypes.COW.id(), EntityTypes.MOOSHROOM.id(), EntityTypes.SHEEP.id()
        )));

        config.setComment("size-variance", "The list of entities to enable the size variance AI mechanic for.");
        sizeVariance = Set.copyOf(config.getStringList("size-variance", List.of(
            EntityTypes.CHICKEN.id(), EntityTypes.PIG.id(), EntityTypes.COW.id(), EntityTypes.MOOSHROOM.id(), EntityTypes.SHEEP.id()
        )));

        config.setComment("size-variance-allow-breeding", "Whether size variance also applies when breeding entities together.");
        sizeVarianceAllowBreeding = config.getBoolean("size-variance-allow-breeding", true);

        config.setComment("size-variance-variability", "The possible variability from default size to apply to the entities.");
        sizeVarianceVariability = config.getDouble("size-variance-variability", 0.2);

        config.setComment("size-variance-breeding-variability", "The possible variability from the bred size to apply while breeding entities.");
        sizeVarianceBreedingVariability = config.getDouble("size-variance-breeding-variability", 0.1);
    }
}
