package com.sk89q.craftbook.mech.dispenser;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Fan extends Recipe{

    public Fan(int[] recipe) {
        super(recipe);
    }

    public Fan() {
        super(new int[]{30, 18, 30, 18, 33, 18, 30, 18, 30});
    }

    @Override
    public boolean doAction(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        MaterialData d = dis.getBlock().getState().getData();
        org.bukkit.material.Dispenser disp = (org.bukkit.material.Dispenser) d;
        BlockFace face = disp.getFacing();
        for(Entity e : dis.getWorld().getChunkAt(dis.getBlock().getRelative(face).getLocation()).getEntities()) {
            if(e.getLocation().getBlock().getLocation().equals(dis.getBlock().getRelative(face).getLocation()) || e.getLocation().getBlock().getLocation().equals(dis.getBlock().getRelative(face).getLocation().subtract(0,1,0))) {
                e.setVelocity(e.getVelocity().add(velocity));
            }
        }
        return true;
    }
}
