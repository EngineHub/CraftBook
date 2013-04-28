package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;

public class BetterPhysics implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockCreatedEntity(EntityChangeBlockEvent event) {

        if(event.getBlock().getTypeId() == BlockID.FLOWER_POT && CraftBookPlugin.inst().getConfiguration().physicsPots)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new ShatteringPots(event.getBlock(), true), 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().physicsEnabled)
            return;

        if(event.getBlock().getTypeId() == BlockID.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()), 1L);

        if(event.getBlock().getTypeId() == BlockID.FLOWER_POT && CraftBookPlugin.inst().getConfiguration().physicsPots)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new ShatteringPots(event.getBlock(), false), 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().physicsEnabled)
            return;

        if(event.getBlock().getTypeId() == BlockID.LADDER && CraftBookPlugin.inst().getConfiguration().physicsLadders)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()), 1L);

        if(event.getBlock().getTypeId() == BlockID.FLOWER_POT && CraftBookPlugin.inst().getConfiguration().physicsPots)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new ShatteringPots(event.getBlock(), false), 1L);
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

    public class ShatteringPots implements Runnable {

        Block pot;
        boolean shatter;

        public ShatteringPots(Block pot, boolean shatter) {

            this.pot = pot;
            this.shatter = shatter;
        }

        @Override
        public void run () {
            if(shatter) {
                pot.setTypeId(0);
                pot.getWorld().playSound(pot.getLocation(), Sound.GLASS, 1.0f, 1.0f);
            } else {
                if(pot.getRelative(0, -1, 0).getTypeId() != 0)
                    return;
                FallingBlock b = pot.getWorld().spawn(pot.getLocation(), FallingBlock.class);
                b.setDropItem(false);
            }
        }
    }
}