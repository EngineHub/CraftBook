package com.sk89q.craftbook.mech.drops;

import org.bukkit.inventory.ItemStack;

public class DropItemStack {

    private ItemStack stack;

    private int chance = 100;
    private int minA = -1, maxA = -1;

    public DropItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {

        return stack;
    }

    public void setChance(int chance) {

        this.chance = chance;
    }

    public int getChance() {

        return chance;
    }

    public void setMinimum(int minA) {

        this.minA = minA;
    }

    public int getMinimum() {

        return minA;
    }

    public void setMaximum(int maxA) {

        this.maxA = maxA;
    }

    public int getMaximum() {

        return maxA;
    }
}