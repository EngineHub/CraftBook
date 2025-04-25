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
import com.sk89q.worldedit.world.block.BlockCategories;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.ConfigUtil;
import org.enginehub.craftbook.util.TernaryState;

import java.util.List;

public abstract class Chairs extends AbstractCraftBookMechanic {
    protected static final double ARMOR_STAND_MOUNT_Y = 2.375;

    public Chairs(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean allowHeldBlock;
    protected TernaryState allowSneaking;
    protected boolean regenHealth;
    protected boolean lowerExhaustion;
    protected double healAmount;
    protected List<BaseBlock> allowedBlocks;
    protected boolean faceWhenPossible;
    protected boolean requireSign;
    protected boolean exitToLastPosition;
    protected int maxSignDistance;
    protected int maxClickRadius;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-holding-blocks", "Allow players to sit in chairs when holding blocks.");
        allowHeldBlock = config.getBoolean("allow-holding-blocks", false);

        config.setComment("allow-sneaking", "Allow players to sit in chairs while sneaking.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.FALSE.toString()));

        config.setComment("regen-health", "Regenerate health passively when seated.");
        regenHealth = config.getBoolean("regen-health", true);

        config.setComment("lower-exhaustion", "Lower the player's exhaustion level when seated.");
        lowerExhaustion = config.getBoolean("lower-exhaustion", true);

        config.setComment("regen-health-amount", "The amount of health regenerated passively. (Can be decimal)");
        healAmount = config.getDouble("regen-health-amount", 1);

        config.setComment("blocks", "A list of blocks that can be sat on.");
        allowedBlocks = BlockParser.getBlocks(config.getStringList("blocks", ConfigUtil.getIdsFromCategory(BlockCategories.STAIRS)), true);

        config.setComment("face-correct-direction", "When the player sits, automatically face them the direction of the chair. (If possible)");
        faceWhenPossible = config.getBoolean("face-correct-direction", true);

        config.setComment("require-sign", "Require a sign to be attached to the chair in order to work!");
        requireSign = config.getBoolean("require-sign", false);

        config.setComment("max-sign-distance", "The maximum distance between the click point and the sign. (When require sign is on)");
        maxSignDistance = config.getInt("max-sign-distance", 3);

        config.setComment("max-click-radius", "The maximum distance the player can be from the sign.");
        maxClickRadius = config.getInt("max-click-radius", 5);

        config.setComment("exit-to-last-position", "Teleport players to their last position when they exit the chair.");
        exitToLastPosition = config.getBoolean("exit-to-last-position", false);
    }
}
