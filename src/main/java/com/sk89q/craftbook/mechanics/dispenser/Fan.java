package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.util.EntityUtil;

/**
 * @author Me4502
 */
public class Fan extends Recipe {

    public Fan(Material[] recipe) {

        super(recipe);
    }

    public Fan() {
        super(new Material[] {
                Material.WEB,    Material.LEAVES,         Material.WEB,
                Material.LEAVES, Material.PISTON_BASE,    Material.LEAVES,
                Material.WEB,    Material.LEAVES,         Material.WEB
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        MaterialData d = dis.getBlock().getState().getData();
        BlockFace face = ((org.bukkit.material.Dispenser) d).getFacing();
        Location dispenserLoc = dis.getBlock().getRelative(face).getLocation();
        for (Entity e : dis.getWorld().getChunkAt(dispenserLoc).getEntities()) {
            if (EntityUtil.isEntityInBlock(e, dispenserLoc.getBlock())) {
                Vector dir = new Vector(((DirectionalContainer) dis.getData()).getFacing().getModX(),((DirectionalContainer) dis.getData()).getFacing().getModY(),((DirectionalContainer) dis.getData()).getFacing().getModZ());
                e.setVelocity(e.getVelocity().add(dir).normalize().multiply(10));
            }
        }
        return true;
    }
}