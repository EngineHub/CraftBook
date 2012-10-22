package com.sk89q.craftbook.mech.dispenser;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class FireArrows extends Recipe {

    public FireArrows(int[] recipe) {

        super(recipe);
    }

    public FireArrows() {

        super(new int[] {
                BlockID.AIR, ItemID.FIRE_CHARGE, BlockID.AIR,
                ItemID.FIRE_CHARGE, ItemID.ARROW, ItemID.FIRE_CHARGE,
                BlockID.AIR, ItemID.FIRE_CHARGE, BlockID.AIR
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        MaterialData d = dis.getBlock().getState().getData();
        org.bukkit.material.Dispenser disp = (org.bukkit.material.Dispenser) d;
        BlockFace face = disp.getFacing();
        Arrow a = dis.getWorld().spawnArrow(dis.getBlock().getRelative(face).getLocation(), velocity, 1.0f, 0.0f);
        a.setFireTicks(5000);
        return true;
    }
}