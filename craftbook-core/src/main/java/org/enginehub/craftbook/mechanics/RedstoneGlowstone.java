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

/**
 * This mechanism allow players to toggle GlowStone.
 */
public abstract class RedstoneGlowstone extends AbstractCraftBookMechanic {

    public RedstoneGlowstone(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean preventBreaking;
    protected BaseBlock offBlock;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("off-block", "Sets the block that the glowstone turns into when turned off.");
        offBlock = BlockParser.getBlock(config.getString("off-block", BlockTypes.SOUL_SAND.id()), true);

        config.setComment("prevent-breaking", "Whether powered Glowstone should be unbreakable.");
        preventBreaking = config.getBoolean("prevent-breaking", false);
    }
}
