package com.sk89q.craftbook.mechanics.drops;

import java.util.List;

import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.util.ItemInfo;

public class BlockCustomDropDefinition extends CustomDropDefinition {

    private ItemInfo blockData;

    /**
     * Instantiate a Block-Type CustomDrop.
     */
    public BlockCustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, ItemInfo blockData) {
        super(name, drops, extraRewards);

        this.blockData = blockData;
    }

    public ItemInfo getBlockType() {
        return blockData;
    }
}