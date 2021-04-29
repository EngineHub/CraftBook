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

package org.enginehub.craftbook.mechanics.drops;

import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.enginehub.craftbook.mechanics.drops.rewards.DropReward;
import org.enginehub.craftbook.util.TernaryState;

import java.util.List;

public class BlockCustomDropDefinition extends CustomDropDefinition {

    private BlockStateHolder blockData;

    /**
     * Instantiate a Block-Type CustomDrop.
     */
    public BlockCustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, TernaryState silkTouch, BlockStateHolder blockData) {
        super(name, drops, extraRewards, silkTouch);

        this.blockData = blockData;
    }

    public BlockStateHolder getBlockType() {
        return blockData;
    }
}