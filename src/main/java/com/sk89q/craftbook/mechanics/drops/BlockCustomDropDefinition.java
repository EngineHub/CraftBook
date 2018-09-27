package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

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