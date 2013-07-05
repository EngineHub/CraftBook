package com.sk89q.craftbook.mech.dispenser;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * @author Me4502
 */
public class SnowShooter extends ItemShooter {

    public SnowShooter(int[] recipe) {

        super(ItemID.SNOWBALL, recipe);
    }

    public SnowShooter() {

        super(ItemID.SNOWBALL, new int[] {
                BlockID.AIR,            BlockID.SNOW_BLOCK,     BlockID.AIR,
                BlockID.SNOW_BLOCK,     ItemID.POTION,          BlockID.SNOW_BLOCK,
                BlockID.AIR,            BlockID.SNOW_BLOCK,     BlockID.AIR
        });
    }
}