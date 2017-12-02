package com.sk89q.craftbook.sponge.mechanics.dispenser;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;

public abstract class SimpleDispenserRecipe implements DispenserRecipe {

    private ItemStack[] recipe;

    public SimpleDispenserRecipe(ItemStack[] recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean doesPass(ItemStack[] checkRecipe) {
        for (int i = 0; i < checkRecipe.length; i++) {
            if (ItemStackComparators.TYPE.compare(recipe[i], checkRecipe[i]) != 0) {
                return false;
            }
        }

        return true;
    }
}
