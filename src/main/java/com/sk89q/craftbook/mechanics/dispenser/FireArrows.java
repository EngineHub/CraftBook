package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
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

    public FireArrows(Material[] recipe) {

        super(recipe);
    }

    public FireArrows() {

        super(new Material[] {
                Material.AIR,            Material.FIREBALL,     Material.AIR,
                Material.FIREBALL,     Material.ARROW,           Material.FIREBALL,
                Material.AIR,            Material.FIREBALL,     Material.AIR
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