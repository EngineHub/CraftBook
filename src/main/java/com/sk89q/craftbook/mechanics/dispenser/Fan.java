package com.sk89q.craftbook.mechanics.dispenser;

import com.sk89q.craftbook.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class Fan extends Recipe {

    public Fan(Material[] recipe) {
        super(recipe);
    }

    public Fan() {
        super(new Material[] {
                Material.COBWEB,    Material.OAK_LEAVES,         Material.COBWEB,
                Material.OAK_LEAVES, Material.PISTON,    Material.OAK_LEAVES,
                Material.COBWEB,    Material.OAK_LEAVES,         Material.COBWEB
        });
    }

    @Override
    public boolean doAction(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        Dispenser d = (Dispenser) block.getBlockData();
        BlockFace face = d.getFacing();
        Location dispenserLoc = block.getRelative(face).getLocation();
        for (Entity e : block.getWorld().getChunkAt(dispenserLoc).getEntities()) {
            if (EntityUtil.isEntityInBlock(e, dispenserLoc.getBlock())) {
                Vector dir = new Vector(d.getFacing().getModX(), d.getFacing().getModY(), d.getFacing().getModZ());
                e.setVelocity(e.getVelocity().add(dir).normalize().multiply(10));
            }
        }
        return true;
    }
}