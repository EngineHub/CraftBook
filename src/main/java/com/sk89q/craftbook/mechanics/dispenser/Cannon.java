package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Cannon extends Recipe {

    public Cannon(Material[] recipe) {

        super(recipe);
    }

    public Cannon() {
        super(new Material[] {
                Material.FIRE_CHARGE,     Material.GUNPOWDER, Material.FIRE_CHARGE,
                Material.GUNPOWDER,         Material.TNT,    Material.GUNPOWDER,
                Material.FIRE_CHARGE,     Material.GUNPOWDER, Material.FIRE_CHARGE
        });
    }

    @Override
    public boolean doAction(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        Directional disp = (Directional) block.getBlockData();
        BlockFace face = disp.getFacing();
        Location location = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        TNTPrimed a = (TNTPrimed) block.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
        a.setVelocity(velocity.normalize().multiply(2));
        return true;
    }
}