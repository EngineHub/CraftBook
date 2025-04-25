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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.TernaryState;

public abstract class XPStorer extends AbstractCraftBookMechanic {

    public XPStorer(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean requireBottle;
    protected boolean allowOffHand;
    protected int bottleXpRequirement;
    protected int bottleXpOverride;
    protected String bottleExtraData;
    protected BaseBlock block;
    protected TernaryState allowSneaking;
    protected boolean radiusMode;
    protected int maxRadius;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("require-bottle", "Requires the player to be holding a glass bottle to use.");
        requireBottle = config.getBoolean("require-bottle", true);

        config.setComment("allow-offhand", "Allows XP bottles in the off hand to work.");
        allowOffHand = config.getBoolean("allow-offhand", true);

        config.setComment("bottle-xp-requirement", "Sets the amount of XP points required per each bottle.");
        bottleXpRequirement = config.getInt("bottle-xp-requirement", 16);

        config.setComment("bottle-xp-override", "Set the amount of XP points that each bottle provides on usage (-1 to use MC behaviour).");
        bottleXpOverride = config.getInt("bottle-xp-override", -1);

        config.setComment("bottle-extra-data", "Extra data to apply to the item, using /give command syntax.");
        bottleExtraData = config.getString("bottle-extra-data", "minecraft:experience_bottle");

        config.setComment("block", "The block that is an XP Storer.");
        block = BlockParser.getBlock(config.getString("block", BlockTypes.SPAWNER.id()), true);

        config.setComment("allow-sneaking", "Sets how the player must be sneaking in order to use the XP Storer.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.FALSE.toString()));

        config.setComment("radius-mode", "Allows XP Storer mechanics with a sign attached to work in a radius.");
        radiusMode = config.getBoolean("radius-mode", false);

        config.setComment("max-radius", "The max radius when using radius-mode.");
        maxRadius = config.getInt("max-radius", 5);
    }
}
