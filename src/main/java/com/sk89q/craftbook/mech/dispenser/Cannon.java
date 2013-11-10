package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
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
                Material.FIREBALL,     Material.SULPHUR, Material.FIREBALL,
                Material.SULPHUR,         Material.TNT,    Material.SULPHUR,
                Material.FIREBALL,     Material.SULPHUR, Material.FIREBALL
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        org.bukkit.material.Dispenser disp = (org.bukkit.material.Dispenser) dis.getData();
        BlockFace face = disp.getFacing();
        Location location = dis.getBlock().getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        TNTPrimed a = (TNTPrimed) dis.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
        a.setVelocity(velocity.normalize().multiply(2));
        return true;
    }
}