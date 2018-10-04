package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class FireArrows extends Recipe {

    public FireArrows(Material[] recipe) {

        super(recipe);
    }

    public FireArrows() {

        super(new Material[] {
                Material.AIR,            Material.FIRE_CHARGE,     Material.AIR,
                Material.FIRE_CHARGE,     Material.ARROW,           Material.FIRE_CHARGE,
                Material.AIR,            Material.FIRE_CHARGE,     Material.AIR
        });
    }

    @Override
    public boolean doAction(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        Dispenser disp = (Dispenser) block.getBlockData();
        BlockFace face = disp.getFacing();
        Location location = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        Arrow a = block.getWorld().spawnArrow(location, velocity, 1.0f, 0.0f);
        a.setFireTicks(5000);
        return true;
    }
}