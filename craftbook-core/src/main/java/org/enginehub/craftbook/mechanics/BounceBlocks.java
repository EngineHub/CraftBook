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

import java.util.List;

public abstract class BounceBlocks extends AbstractCraftBookMechanic {

    public BounceBlocks(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected List<BaseBlock> allowedBlocks;
    protected double sensitivity;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("blocks", "A list of blocks that can be used as bounce blocks.");
        allowedBlocks = BlockParser.getBlocks(config.getStringList("blocks", ConfigUtil.getIdsFromCategory(BlockCategories.TERRACOTTA)), true);

        config.setComment("sensitivity", "The sensitivity of movement required to activate the block.");
        sensitivity = config.getDouble("sensitivity", 0.1);
    }
}
