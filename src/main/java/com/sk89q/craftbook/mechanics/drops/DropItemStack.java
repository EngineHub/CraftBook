package com.sk89q.craftbook.mechanics.drops;

import org.bukkit.inventory.ItemStack;

public class DropItemStack {

    private ItemStack stack;

    private double chance = 100d;
    private int minA = -1, maxA = -1;
    private String name;

    public DropItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setChance(double chance) {

        this.chance = chance;
    }

    public double getChance() {

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

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}