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
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

public class BetterSponge extends AbstractCraftBookMechanic implements Listener {

    public BetterSponge(MechanicType<? extends CraftBookMechanic> mechanicType) {
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

                    if (isValidSponge(sponge.getType()) && (!redstone || sponge.isBlockIndirectlyPowered())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isValidSponge(event.getBlock().getType())) {
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
        if (!isValidSponge(event.getBlock().getType())) {
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

        if (!isValidSponge(event.getBlock().getType())) {
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

    private boolean isValidSponge(Material type) {
        return type == Material.SPONGE || includeWet && type == Material.WET_SPONGE;
    }

    private boolean isRemovableWater(Material material) {
        return material == Material.WATER || destructive && (material == Material.SEAGRASS
            || material == Material.TALL_SEAGRASS || material == Material.KELP_PLANT
            || material == Material.KELP);
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
                    if (isRemovableWater(water.getType())) {
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
                if (isRemovableWater(water.getType())) {
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
                if (isRemovableWater(water.getType())) {
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
                if (isRemovableWater(water.getType())) {
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
                if (isRemovableWater(water.getType())) {
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
                if (isRemovableWater(water.getType())) {
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
                if (isRemovableWater(water.getType())) {
                    setBlockToWater(water.getRelative(0, 0, -1), water);
                }
            }
        }
    }

    private int radius;
    private boolean sphereRange;
    private boolean redstone;
    private boolean includeWet;
    private boolean destructive;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("radius", "The maximum radius of the sponge.");
        radius = config.getInt("radius", 5);

        config.setComment("sphere-range", "Whether the active range should be spherical or cuboid.");
        sphereRange = config.getBoolean("sphere-range", true);

        config.setComment("include-wet-sponges", "Whether wet sponges also activate the mechanic.");
        includeWet = config.getBoolean("include-wet-sponges", false);

        config.setComment("require-redstone", "Whether to require redstone to suck up water or not.");
        redstone = config.getBoolean("require-redstone", false);

        config.setComment("destructive", "Whether to remove blocks that spread water such as kelp. These will not be returned when the sponge is de-activated.");
        destructive = config.getBoolean("destructive", true);
    }
}
