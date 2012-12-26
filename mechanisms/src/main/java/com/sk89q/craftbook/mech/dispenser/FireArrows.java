package com.sk89q.craftbook.mech.dispenser;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
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
                BlockID.AIR, ItemID.FIRE_CHARGE, BlockID.AIR, ItemID.FIRE_CHARGE, ItemID.ARROW,
                ItemID.FIRE_CHARGE, BlockID.AIR,
                ItemID.FIRE_CHARGE, BlockID.AIR
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        org.bukkit.material.Dispenser disp = (org.bukkit.material.Dispenser) dis.getData();
        BlockFace face = disp.getFacing();
        Location location = dis.getBlock().getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        Arrow a = dis.getWorld().spawnArrow(location, velocity, 1.0f, 0.0f);
        a.setFireTicks(5000);
        return true;
    }
}