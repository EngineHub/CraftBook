package com.sk89q.craftbook.mech.dispenser;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class Fan extends Recipe {

    public Fan(int[] recipe) {

        super(recipe);
    }

    public Fan() {

        super(new int[] {BlockID.WEB, BlockID.LEAVES, BlockID.WEB, BlockID.LEAVES, BlockID.PISTON_BASE,
                BlockID.LEAVES, BlockID.WEB, BlockID.LEAVES,
                BlockID.WEB});
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        MaterialData d = dis.getBlock().getState().getData();
        BlockFace face = ((org.bukkit.material.Dispenser) d).getFacing();
        Location dispenserLoc = dis.getBlock().getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        for (Entity e : dis.getWorld().getChunkAt(dispenserLoc).getEntities()) {
            if (e.getLocation().distanceSquared(dispenserLoc) <= 2 * 2) {
                e.setVelocity(e.getVelocity().add(velocity).multiply(10));
            }
        }
        return true;
    }
}