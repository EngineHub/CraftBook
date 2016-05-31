package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.TernaryState;

import java.util.List;

public class BlockCustomDropDefinition extends CustomDropDefinition {

    private ItemInfo blockData;

    /**
     * Instantiate a Block-Type CustomDrop.
     */
    public BlockCustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, TernaryState silkTouch, ItemInfo blockData) {
        super(name, drops, extraRewards, silkTouch);

        this.blockData = blockData;
    }

    public ItemInfo getBlockType() {
        return blockData;
    }
}