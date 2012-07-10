package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FireArrows extends Recipe {

    public FireArrows(int[] recipe) {
        super(recipe);
    }

    public FireArrows() {
        super(new int[]{0, 385, 0, 385, 262, 385, 0, 385, 0});
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        Arrow a = dis.getWorld().spawnArrow(dis.getLocation(), velocity, 2.0f, 0.0f);
        a.setFireTicks(5000);
        return true;
    }
}