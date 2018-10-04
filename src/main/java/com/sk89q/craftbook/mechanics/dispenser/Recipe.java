package com.sk89q.craftbook.mechanics.dispenser;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public abstract class Recipe {

    private final Material[] recipe;

    public Recipe(Material[] materials) {

        recipe = materials.clone();
    }

    /**
     * Does the recipe action.
     *
     * @param block    the dispenser firing the item
     * @param item     the original item to be fired
     * @param velocity the velocity the item is to be fired at
     * @param event    the BlockDispenseEvent
     *
     * @return true if event needs to be cancelled.
     */
    public abstract boolean doAction(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event);

    /**
     * Gets the contents of this recipe as a 9-element array representing the 3x3 dispenser grid.
     *
     * @return the recipe contents
     */
    public Material[] getRecipe() {

        return recipe;
    }

    @Override
    public boolean equals(Object o) {

        return this == o || o instanceof Recipe && Arrays.equals(recipe, ((Recipe) o).recipe);

    }

    @Override
    public int hashCode() {

        return Arrays.hashCode(recipe);
    }
}