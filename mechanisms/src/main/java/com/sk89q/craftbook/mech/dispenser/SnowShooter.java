package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class SnowShooter extends Recipe {

    public SnowShooter(int[] recipe) {

        super(recipe);
    }

    public SnowShooter() {

        super(new int[] {0, 80, 0, 80, 373, 80, 0, 80, 0});
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        event.setItem(new ItemStack(332, 1));
        return true;
    }
}