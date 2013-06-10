package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;

public class BetterPhysics implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if(event.getBlock().getTypeId() == BlockID.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()), 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if(event.getBlock().getTypeId() == BlockID.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()), 1L);
    }

    public class FallingLadders implements Runnable {

        Block ladder;

        public FallingLadders(Block ladder) {

            this.ladder = ladder;
        }

        @Override
        public void run () {
            if(ladder.getRelative(0, -1, 0).getTypeId() != 0)
                return;
            ladder.getWorld().spawn(ladder.getLocation(), FallingBlock.class);
        }
    }
}