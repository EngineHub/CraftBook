/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class BetterPlants extends AbstractCraftBookMechanic {

    private @Nullable BukkitTask growthTask;

    public BetterPlants(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        if (fernFarming) {
            growthTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new GrowthTicker(), 2L, 2L);
        }
    }

    @Override
    public void disable() {
        if (growthTask != null) {
            growthTask.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (fernFarming) {
            handleFernBreak(event.getBlock());
        }
    }

    private void handleFernBreak(Block block) {
        Material type = block.getType();
        if (type != Material.LARGE_FERN) {
            return;
        }

        Bisected data = (Bisected) block.getBlockData();
        if (data.getHalf() != Bisected.Half.TOP) {
            return;
        }

        Block below = block.getRelative(BlockFace.DOWN);
        Material belowType = below.getType();
        if (belowType != Material.LARGE_FERN) {
            return;
        }

        Bisected belowData = (Bisected) below.getBlockData();
        if (belowData.getHalf() != Bisected.Half.BOTTOM) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
            block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), new ItemStack(Material.FERN));
            below.setType(Material.FERN);
        }, 2L);
    }

    private class GrowthTicker implements Runnable {
        @Override
        public void run() {
            if (Bukkit.getServer().isPaused()) {
                // Skip growth ticks when the server is paused
                return;
            }

            for (World world : Bukkit.getWorlds()) {
                int x = 0;
                int y = 0;
                int z = 0;

                if (fastTickRandoms) {
                    x = ThreadLocalRandom.current().nextInt(16);
                    y = ThreadLocalRandom.current().nextInt(world.getMaxHeight());
                    z = ThreadLocalRandom.current().nextInt(16);
                }

                for (Chunk chunk : world.getLoadedChunks()) {
                    Block block;
                    if (!fastTickRandoms) {
                        x = ThreadLocalRandom.current().nextInt(16);
                        y = ThreadLocalRandom.current().nextInt(world.getMaxHeight());
                        z = ThreadLocalRandom.current().nextInt(16);
                    }

                    block = chunk.getBlock(x, y, z);

                    if (fernFarming && block.getType() == Material.FERN) {
                        Block aboveBlock = block.getRelative(0, 1, 0);
                        if (BlockUtil.isBlockReplacable(aboveBlock.getType())) {
                            block.setType(Material.LARGE_FERN, false);
                            Bisected topHalfData = ((Bisected) Material.LARGE_FERN.createBlockData());
                            topHalfData.setHalf(Bisected.Half.TOP);
                            aboveBlock.setBlockData(topHalfData, false);
                        }
                    }
                }
            }
        }
    }

    private boolean fernFarming;
    private boolean fastTickRandoms;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("fern-farming", "Allows ferns to be farmed by breaking top half of a large fern. (And small ferns to grow)");
        fernFarming = config.getBoolean("fern-farming", true);

        config.setComment("fast-random-ticks", "Use a way of generating less random numbers, by only generating it once for all chunks, instead of one each chunk.");
        fastTickRandoms = config.getBoolean("fast-random-ticks", true);
    }
}
