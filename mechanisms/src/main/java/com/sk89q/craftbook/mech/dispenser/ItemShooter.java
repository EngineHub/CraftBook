package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ItemShooter extends Recipe {

    private final int itemId;

    public ItemShooter(int id, int[] recipe) {

        super(recipe);
        this.itemId = id;
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        event.setItem(new ItemStack(itemId, 1));
        return true;
    }
}
