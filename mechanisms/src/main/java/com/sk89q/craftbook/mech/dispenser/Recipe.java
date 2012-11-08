package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;

/**
 * @author Me4502
 */
public abstract class Recipe {

    private final int[] recipe;

    public Recipe(int[] recipe) {

        this.recipe = recipe.clone();
    }

    /**
     * Does the recipe action.
     *
     * @param dis the dispenser firing the item
     * @param item the original item to be fired
     * @param velocity the velocity the item is to be fired at
     * @param event the BlockDispenseEvent
     *
     * @return true if event needs to be cancelled.
     */
    public abstract boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event);

    /**
     * Gets the contents of this recipe as a 9-element array representing the 3x3 dispenser grid.
     * @return the recipe contents
     */
    public int[] getRecipe() {
        return recipe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe)) return false;

        return Arrays.equals(recipe, ((Recipe) o).recipe);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(recipe);
    }
}