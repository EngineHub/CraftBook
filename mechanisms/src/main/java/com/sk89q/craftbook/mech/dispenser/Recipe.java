package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class Recipe {

    final int[] recipe;

    public Recipe(int[] recipe) {

        this.recipe = recipe.clone();
    }

    /**
     * Does the recipe action.
     *
     * @param velocity
     * @param event
     *
     * @return true if event needs to be cancelled.
     */
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        return false;
    }
}