package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * @author Me4502
 */
public class SnowShooter extends Recipe {

    public SnowShooter(int[] recipe) {

        super(recipe);
    }

    public SnowShooter() {

        super(new int[] {
                BlockID.AIR,            BlockID.SNOW_BLOCK,     BlockID.AIR,
                BlockID.SNOW_BLOCK,     ItemID.POTION,          BlockID.SNOW_BLOCK,
                BlockID.AIR,            BlockID.SNOW_BLOCK,     BlockID.AIR
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        event.setItem(new ItemStack(332, 1));
        return true;
    }
}