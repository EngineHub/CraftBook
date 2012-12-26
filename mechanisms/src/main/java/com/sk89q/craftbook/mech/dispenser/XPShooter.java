package com.sk89q.craftbook.mech.dispenser;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * @author Me4502
 */
public class XPShooter extends ItemShooter {

    public XPShooter(int[] recipe) {

        super(ItemID.BOTTLE_O_ENCHANTING, recipe);
    }

    public XPShooter() {

        super(ItemID.BOTTLE_O_ENCHANTING, new int[] {BlockID.AIR, ItemID.REDSTONE_DUST, BlockID.AIR,
                ItemID.REDSTONE_DUST, ItemID.GLASS_BOTTLE,
                ItemID.REDSTONE_DUST, BlockID.AIR, ItemID.REDSTONE_DUST, BlockID.AIR});
    }
}