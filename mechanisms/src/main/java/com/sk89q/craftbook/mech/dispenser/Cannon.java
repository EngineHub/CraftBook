package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class Cannon extends Recipe {

    public Cannon(int[] recipe) {

        super(recipe);
    }

    public Cannon() {

        super(new int[] {
                ItemID.FIRE_CHARGE, ItemID.SULPHUR, ItemID.FIRE_CHARGE,
                ItemID.SULPHUR, BlockID.TNT, ItemID.SULPHUR,
                ItemID.FIRE_CHARGE, ItemID.SULPHUR, ItemID.FIRE_CHARGE
        });
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        MaterialData d = dis.getBlock().getState().getData();
        org.bukkit.material.Dispenser disp = (org.bukkit.material.Dispenser) d;
        BlockFace face = disp.getFacing();
        TNTPrimed a = (TNTPrimed) dis.getWorld().spawnEntity(dis.getBlock().getRelative(face).getLocation(),
                EntityType.PRIMED_TNT);
        a.setVelocity(velocity);
        a.setIsIncendiary(true);
        a.setYield(0.4f);
        a.setFuseTicks(200);
        return true;
    }
}