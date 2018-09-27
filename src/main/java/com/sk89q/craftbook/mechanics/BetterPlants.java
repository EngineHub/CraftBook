package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class BetterPlants extends AbstractCraftBookMechanic {

    private BukkitTask growthTask;

    @Override
    public boolean enable() {
        if(fernFarming) {
            tickedWorlds.addAll(Bukkit.getWorlds());

            growthTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new GrowthTicker(), 2L, 2L);
        }

        return fernFarming; //Only enable if a mech is enabled
    }

    @Override
    public void disable() {
        tickedWorlds.clear();

        if(growthTask != null)
            growthTask.cancel();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if(fernFarming && event.getBlock().getType() == Material.LARGE_FERN
                && ((Bisected) event.getBlock().getBlockData()).getHalf() == Bisected.Half.TOP
                && event.getBlock().getRelative(0, -1, 0).getType() == Material.LARGE_FERN
                && ((Bisected) event.getBlock().getRelative(0, -1, 0).getBlockData()).getHalf() == Bisected.Half.BOTTOM) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), new ItemStack(Material.FERN));
                event.getBlock().getRelative(0, -1, 0).setType(Material.FERN);
            }, 2L);
        }
    }

    private class GrowthTicker implements Runnable {

        @Override
        public void run () {

            for(World world : tickedWorlds) {

                int x = 0, y = 0, z = 0;

                if(fastTickRandoms) {
                    x = CraftBookPlugin.inst().getRandom().nextInt(16);
                    y = CraftBookPlugin.inst().getRandom().nextInt(world.getMaxHeight());
                    z = CraftBookPlugin.inst().getRandom().nextInt(16);
                }

                for(Chunk chunk : world.getLoadedChunks()) {
                    Block block;
                    if(fastTickRandoms)
                        block = chunk.getBlock(x,y,z);
                    else
                        block = chunk.getBlock(CraftBookPlugin.inst().getRandom().nextInt(16), CraftBookPlugin.inst().getRandom().nextInt(world.getMaxHeight()), CraftBookPlugin.inst().getRandom().nextInt(16));

                    if(fernFarming && block.getType() == Material.FERN) {
                        block.setType(Material.LARGE_FERN, false);
                        Bisected topHalfData = ((Bisected) Material.LARGE_FERN.createBlockData());
                        topHalfData.setHalf(Bisected.Half.TOP);
                        block.getRelative(0, 1, 0).setBlockData(topHalfData, false);
                    }
                }
            }
        }
    }

    private Set<World> tickedWorlds = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {

        tickedWorlds.add(event.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldUnload(WorldUnloadEvent event) {

        tickedWorlds.remove(event.getWorld());
    }

    private boolean fernFarming;
    private boolean fastTickRandoms;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "fern-farming", "Allows ferns to be farmed by breaking top half of a large fern. (And small ferns to grow)");
        fernFarming = config.getBoolean(path + "fern-farming", true);

        config.setComment(path + "fast-random-ticks", "Use a way of generating less random numbers, by only generating it once for all chunks, instead of one each chunk.");
        fastTickRandoms = config.getBoolean(path + "fast-random-ticks", true);
    }
}