/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

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