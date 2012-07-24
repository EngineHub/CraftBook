package com.sk89q.craftbook.mech.dispenser;

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

        super(new int[] {0, 331, 0, 331, 374, 331, 0, 331, 0});
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        event.setItem(new ItemStack(384, 1));
        return true;
    }
}