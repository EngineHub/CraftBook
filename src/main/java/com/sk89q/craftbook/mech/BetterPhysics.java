package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;

public class BetterPhysics implements Listener {

    public BetterPhysics() {

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().physicsEnabled)
            return;

        if(event.getBlockPlaced().getTypeId() == BlockID.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders) {

            if(fallingLadders(event.getBlockPlaced()))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().physicsEnabled)
            return;

        if(event.getBlock().getTypeId() == BlockID.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders) {

            if(fallingLadders(event.getBlock()))
                event.setCancelled(true);
        }
    }

    public boolean fallingLadders(Block ladder) {

        if(ladder.getRelative(0, -1, 0).getTypeId() != 0)
            return false;
        ladder.getWorld().spawnFallingBlock(ladder.getLocation(), ladder.getType(), ladder.getData());
        ladder.setTypeId(0);
        return true;
    }
}