package com.sk89q.craftbook.mech.dispenser;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class XPShooter extends Recipe {

    public XPShooter(int[] recipe) {

        super(recipe);
    }

    public XPShooter() {

        super(new int[] {
                BlockID.AIR, ItemID.REDSTONE_DUST, BlockID.AIR,
                ItemID.REDSTONE_DUST, ItemID.GLASS_BOTTLE, ItemID.REDSTONE_DUST,
                BlockID.AIR, ItemID.REDSTONE_DUST, BlockID.AIR
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        event.setItem(new ItemStack(384, 1));
        return true;
    }
}