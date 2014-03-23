package com.sk89q.craftbook.mech;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;

public class BetterPlants extends AbstractCraftBookMechanic {

    @Override
    public boolean enable() {

        for(World world : Bukkit.getWorlds())
            tickedWorlds.add(world);

        if(CraftBookPlugin.inst().getConfiguration().betterPlantsFernFarming)
            Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new GrowthTicker(), 2L, 2L);

        return CraftBookPlugin.inst().getConfiguration().betterPlantsFernFarming; //Only enable if a mech is enabled
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {

        if(CraftBookPlugin.inst().getConfiguration().betterPlantsFernFarming && event.getBlock().getType() == Material.DOUBLE_PLANT && event.getBlock().getData() >= 0x8 && event.getBlock().getRelative(0, -1, 0).getType() == Material.DOUBLE_PLANT && event.getBlock().getRelative(0, -1, 0).getData() == 0x3) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {
                @Override
                public void run () {
                    event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), new ItemStack(Material.LONG_GRASS, 1, (short) 2));
                    event.getBlock().getRelative(0, -1, 0).setTypeIdAndData(Material.LONG_GRASS.getId(), (byte) 2, true);
                }
            }, 2L);
        }
    }

    public class GrowthTicker extends BukkitRunnable {

        @Override
        public void run () {

            for(World world : tickedWorlds) {
                for(Chunk chunk : world.getLoadedChunks()) {
                    for(int i = 0; i < 16; i++) {
                        Block block = chunk.getBlock(CraftBookPlugin.inst().getRandom().nextInt(16), CraftBookPlugin.inst().getRandom().nextInt(world.getMaxHeight()), CraftBookPlugin.inst().getRandom().nextInt(16));

                        if(CraftBookPlugin.inst().getConfiguration().betterPlantsFernFarming && block.getType() == Material.LONG_GRASS && block.getData() == 0x2) {
                            block.setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 3, false);
                            block.getRelative(0, 1, 0).setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 11, false);
                        }
                    }
                }
            }
        }
    }

    private Set<World> tickedWorlds = new HashSet<World>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {

        tickedWorlds.add(event.getWorld());
    }

    public void onWorldUnload(WorldUnloadEvent event) {

        tickedWorlds.remove(event.getWorld());
    }
}