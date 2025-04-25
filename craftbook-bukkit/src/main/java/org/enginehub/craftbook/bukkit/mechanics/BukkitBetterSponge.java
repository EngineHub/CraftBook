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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.BetterSponge;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

public class BukkitBetterSponge extends BetterSponge implements Listener {

    public BukkitBetterSponge(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().getType() != Material.WATER) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block sponge = event.getToBlock().getRelative(cx, cy, cz);
                    if (sphereRange && !LocationUtil.isWithinSphericalRadius(sponge, event.getToBlock(), radius)) {
                        continue;
                    }

                    BlockType blockType = BukkitAdapter.asBlockType(sponge.getType());

                    if (isValidSponge(blockType) && (!redstone || sponge.isBlockIndirectlyPowered())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockType blockType = BukkitAdapter.asBlockType(event.getBlock().getType());

        if (!isValidSponge(blockType)) {
            return;
        }

        if (redstone && !event.getBlock().isBlockIndirectlyPowered()) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        removeWater(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        BlockType blockType = BukkitAdapter.asBlockType(event.getBlock().getType());

        if (!isValidSponge(blockType)) {
            return;
        }

        if (redstone && !event.getBlock().isBlockIndirectlyPowered()) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> addWater(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!redstone || event.isMinor()) {
            return;
        }

        BlockType blockType = BukkitAdapter.asBlockType(event.getBlock().getType());

        if (!isValidSponge(blockType)) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!event.isOn()) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> addWater(event.getBlock()));
        } else {
            removeWater(event.getBlock());
        }
    }

    private void setBlockToWater(Block block, Block source) {
        if (block.getType().isAir()) {
            BlockData sourceData = source.getBlockData();
            int level = 0;
            if (sourceData instanceof Levelled levelled) {
                int sourceLevel = levelled.getLevel();
                if (sourceLevel != 0) {
                    level = Math.max(levelled.getLevel() + 1, 7);
                }
            }
            if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
                BlockFromToEvent event = new BlockFromToEvent(source, block);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    Levelled data = (Levelled) Bukkit.createBlockData(Material.WATER);
                    data.setLevel(level);
                    block.setBlockData(data);
                }
            } else {
                Levelled data = (Levelled) Bukkit.createBlockData(Material.WATER);
                data.setLevel(level);
                block.setBlockData(data);
            }
        }
    }

    public void removeWater(Block block) {
        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block water = block.getRelative(cx, cy, cz);
                    if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius)) {
                        continue;
                    }

                    BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                    if (isRemovableWater(waterType)) {
                        water.setType(Material.AIR);
                    } else {
                        BlockData data = water.getBlockData();
                        if (data instanceof Waterlogged waterlogged) {
                            waterlogged.setWaterlogged(false);
                            water.setBlockData(waterlogged);
                        }
                    }
                }
            }
        }
    }

    public void addWater(Block block) {
        // The negative x edge
        int cx = -radius - 1;
        for (int cy = -radius - 1; cy <= radius + 1; cy++) {
            for (int cz = -radius - 1; cz <= radius + 1; cz++) {
                Block water = block.getRelative(cx, cy, cz);
                if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius + 1)) {
                    continue;
                }

                BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                if (isRemovableWater(waterType)) {
                    setBlockToWater(water.getRelative(1, 0, 0), water);
                }
            }
        }

        // The positive x edge
        cx = radius + 1;
        for (int cy = -radius - 1; cy <= radius + 1; cy++) {
            for (int cz = -radius - 1; cz <= radius + 1; cz++) {
                Block water = block.getRelative(cx, cy, cz);
                if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius + 1)) {
                    continue;
                }

                BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                if (isRemovableWater(waterType)) {
                    setBlockToWater(water.getRelative(-1, 0, 0), water);
                }
            }
        }

        // The negative y edge
        int cy = -radius - 1;
        for (cx = -radius - 1; cx <= radius + 1; cx++) {
            for (int cz = -radius - 1; cz <= radius + 1; cz++) {
                Block water = block.getRelative(cx, cy, cz);
                if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius + 1)) {
                    continue;
                }

                BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                if (isRemovableWater(waterType)) {
                    setBlockToWater(water.getRelative(0, 1, 0), water);
                }
            }
        }

        // The positive y edge
        cy = radius + 1;
        for (cx = -radius - 1; cx <= radius + 1; cx++) {
            for (int cz = -radius - 1; cz <= radius + 1; cz++) {
                Block water = block.getRelative(cx, cy, cz);
                if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius + 1)) {
                    continue;
                }

                BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                if (isRemovableWater(waterType)) {
                    setBlockToWater(water.getRelative(0, -1, 0), water);
                }
            }
        }

        // The negative z edge
        int cz = -radius - 1;
        for (cx = -radius - 1; cx <= radius + 1; cx++) {
            for (cy = -radius - 1; cy <= radius + 1; cy++) {
                Block water = block.getRelative(cx, cy, cz);
                if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius + 1)) {
                    continue;
                }

                BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                if (isRemovableWater(waterType)) {
                    setBlockToWater(water.getRelative(0, 0, 1), water);
                }
            }
        }

        // The positive z edge
        cz = radius + 1;
        for (cx = -radius - 1; cx <= radius + 1; cx++) {
            for (cy = -radius - 1; cy <= radius + 1; cy++) {
                Block water = block.getRelative(cx, cy, cz);
                if (sphereRange && !LocationUtil.isWithinSphericalRadius(water, block, radius + 1)) {
                    continue;
                }

                BlockType waterType = BukkitAdapter.asBlockType(water.getType());

                if (isRemovableWater(waterType)) {
                    setBlockToWater(water.getRelative(0, 0, -1), water);
                }
            }
        }
    }
}
