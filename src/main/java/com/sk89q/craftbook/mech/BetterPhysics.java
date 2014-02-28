package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;

public class BetterPhysics extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(event.getBlock().getType() == Material.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if(event.getBlock().getType() == Material.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockUpdate(BlockPhysicsEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(event.getBlock().getType() == Material.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()));
    }

    public static class FallingLadders implements Runnable {

        Block ladder;

        public FallingLadders(Block ladder) {

            this.ladder = ladder;
        }

        @Override
        public void run () {
            if(ladder.getRelative(0, -1, 0).getType() != Material.AIR)
                return;
            ladder.getWorld().spawnFallingBlock(ladder.getLocation(), ladder.getType(), ladder.getData());
            ladder.setTypeId(Material.AIR.getId(), false);
        }
    }
}