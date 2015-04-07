package com.sk89q.craftbook.mechanics;

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
import com.sk89q.util.yaml.YAMLProcessor;

public class BetterPhysics extends AbstractCraftBookMechanic {

    protected static BetterPhysics instance;

    @Override
    public boolean enable() {

        instance = this;
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(FallingLadders.isValid(event.getBlock()))
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if(FallingLadders.isValid(event.getBlock()))
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockUpdate(BlockPhysicsEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(FallingLadders.isValid(event.getBlock()))
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(event.getBlock()));
    }

    public static class FallingLadders implements Runnable {

        Block ladder;

        public FallingLadders(Block ladder) {

            this.ladder = ladder;
        }

        public static boolean isValid(Block block) {

            return block.getType() == Material.LADDER && instance.ladders && block.getRelative(0, -1, 0).getType() == Material.AIR;
        }

        @Override
        public void run () {

            if(!isValid(ladder)) return;
            ladder.getWorld().spawnFallingBlock(ladder.getLocation(), ladder.getType(), ladder.getData());
            ladder.setTypeId(Material.AIR.getId(), false);
        }
    }

    boolean ladders;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "falling-ladders", "Enables BetterPhysics Falling Ladders.");
        ladders = config.getBoolean(path + "falling-ladders", true);
    }
}