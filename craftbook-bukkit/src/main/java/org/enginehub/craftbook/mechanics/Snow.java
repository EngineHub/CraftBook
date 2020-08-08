/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

import com.google.common.collect.Lists;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.BlockSyntax;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Snow fall mechanism. Builds up/tramples snow
 */
public class Snow extends AbstractCraftBookMechanic {

    private static final double SNOW_MELTING_TEMPERATURE = 0.05D;
    private static final double SNOW_FORM_TEMPERATURE = 0.15D;

    private static final BlockFace[] UPDATE_FACES = {BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final BlockFace[] DISPERSE_FACES = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    private BukkitTask randomTickTask;
    private BukkitTask dispersionTask;
    private DispersionQueue dispersionQueueRunner;

    @Override
    public boolean enable() {
        if (meltSunlight || snowPiling) {
            randomTickTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new SnowRandomTicker(), 20L, 20L);
        }
        if (dispersionMode) {
            dispersionTask = Bukkit.getScheduler().runTaskTimer(
                    CraftBookPlugin.inst(),
                    dispersionQueueRunner = new DispersionQueue(),
                    dispersionTickSpeed,
                    dispersionTickSpeed
            );
        }

        return true;
    }

    @Override
    public void disable() {
        super.disable();

        if (randomTickTask != null) {
            randomTickTask.cancel();
        }
        if (dispersionTask != null) {
            dispersionTask.cancel();
            while (!dispersionQueue.isEmpty()) {
                dispersionQueueRunner.run();
            }
        }
        dispersionQueue.clear();
    }

    private boolean isReplacable(Block block) {
        return !(block.getType() == Material.WATER || block.getType() == Material.LAVA)
                && (BlockUtil.isBlockReplacable(block.getType())
                || Blocks.containsFuzzy(dispersionReplacables, BukkitAdapter.adapt(block.getBlockData())));
    }

    private boolean canLandOn(Block block) {
        Material type = block.getType();
        if (freezeWater && type == Material.WATER) {
            return true;
        }
        return type != Material.ICE && (type == Material.SNOW || block.getPistonMoveReaction() != PistonMoveReaction.BREAK);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!snowballPlacement || !EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getEntity() instanceof Snowball) {
            Block block = event.getEntity().getLocation().getBlock();

            if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
                CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getEntity().getShooter());
                if (!player.hasPermission("craftbook.snow.place")) {
                    return;
                }

                if (!ProtectionUtil.canBuild((Player)event.getEntity().getShooter(), block.getLocation(), true)) {
                    return;
                }
            }

            increaseSnow(block, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!dispersionMode || event.getBlock().getType() != Material.SNOW || !EventUtil.passesFilter(event)) {
            return;
        }

        addToDispersionQueue(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!trample && !slowdown) return;

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        Block toBlock = event.getTo().getBlock();

        if (toBlock.getType() == Material.SNOW) {
            org.bukkit.block.data.type.Snow levelled = (org.bukkit.block.data.type.Snow) toBlock.getBlockData();

            if(slowdown) {
                if (levelled.getLayers() > 4) {
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2));
                } else if (levelled.getLayers() > levelled.getMinimumLayers()) {
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1));
                }
            }

            if (trample) {
                if (jumpTrample && !(event.getFrom().getY() - event.getTo().getY() >= 0.1D)) {
                    return;
                }

                if (ThreadLocalRandom.current().nextInt(20) == 0) {
                    if (partialTrample && levelled.getLayers() == levelled.getMinimumLayers() && toBlock.getRelative(BlockFace.DOWN).getType() != Material.SNOW) {
                        return;
                    }

                    if (!player.hasPermission("craftbook.snow.trample")) {
                        return;
                    }

                    if (!ProtectionUtil.canBuild(event.getPlayer(), event.getPlayer().getLocation(), false)) {
                        return;
                    }

                    decreaseSnow(toBlock, true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!dispersionMode || !EventUtil.passesFilter(event)) {
            return;
        }

        for (BlockFace dir : UPDATE_FACES) {
            addToDispersionQueue(event.getBlock().getRelative(dir));
        }
    }

    boolean increaseSnow(Block snow, boolean disperse) {
        while (snow.getRelative(BlockFace.DOWN).getType().isAir()) {
            snow = snow.getRelative(BlockFace.DOWN);
        }

        Block below = snow.getRelative(BlockFace.DOWN);
        Material belowType = below.getType();

        if(isReplacable(below)) {
            if (belowType != Material.SNOW) {
                return increaseSnow(below, disperse);
            } else {
                org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) below.getBlockData();
                if (snowData.getLayers() < snowData.getMaximumLayers()) {
                    return increaseSnow(below, disperse);
                }
            }
        }

        if (belowType == Material.WATER) {
            if (freezeWater) {
                Levelled waterData = (Levelled) below.getBlockData();
                if (waterData.getLevel() == 0) {
                    BlockState state = below.getState();
                    state.setType(Material.ICE);
                    if (ProtectionUtil.canBlockForm(state.getBlock(), state)) {
                        below.setType(Material.ICE);
                    }
                } else {
                    below.setType(Material.SNOW);
                }
            }

            // Always return true here to prevent snow dancing across lakes
            // If it's not freezing the water, the water is melting the snow
            return true;
        }

        if(snow.getType() != Material.SNOW) {
            if (belowType == Material.SNOW) {
                boolean allowed = pileHigh && below.getRelative(BlockFace.DOWN, maxPileHeight).getType() != Material.SNOW;
                if (!allowed) {
                    return false;
                }
            }

            if (!canLandOn(below)) {
                // This block is non-solid. The snow will just break due to physics.
                return false;
            }

            if(isReplacable(snow)) {
                snow.setType(Material.SNOW, false);
                if (disperse && belowType == Material.SNOW) {
                    addToDispersionQueue(snow);
                }
                return true;
            } else {
                return false;
            }
        }

        org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) snow.getBlockData();
        if(snowData.getLayers() + 1 > snowData.getMaximumLayers()) {
            if(pileHigh && below.getRelative(BlockFace.DOWN, maxPileHeight).getType() != Material.SNOW) {
                snow.setType(Material.SNOW, false);
                if (disperse) {
                    addToDispersionQueue(snow);
                }
                return true;
            } else {
                return false;
            }
        } else {
            snowData.setLayers(snowData.getLayers() + 1);
            snow.setBlockData(snowData);

            if(disperse) {
                addToDispersionQueue(snow);
            }
        }

        return true;
    }

    boolean decreaseSnow(Block snow, boolean disperse) {
        if (snow.getType() != Material.SNOW) {
            return false;
        }

        Block aboveBlock = snow.getRelative(BlockFace.UP);
        if (aboveBlock.getType() == Material.SNOW) {
            return decreaseSnow(aboveBlock, disperse);
        }

        org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) snow.getBlockData();
        if (snowData.getLayers() == snowData.getMinimumLayers()) {
            snow.setType(Material.AIR);
        } else {
            snowData.setLayers(snowData.getLayers() - 1);
        }
        snow.setBlockData(snowData);

        if (disperse) {
            for (BlockFace dir : UPDATE_FACES) {
                addToDispersionQueue(snow.getRelative(dir));
            }
        }

        return true;
    }

    private final Queue<Block> dispersionQueue = new LinkedList<>();

    void addToDispersionQueue(Block block) {
        if (!dispersionMode) {
            return;
        }

        dispersionQueue.offer(block);
    }

    void disperse(Block snow) {
        Material snowType = snow.getType();
        if (snowType != Material.SNOW) {
            return;
        }

        org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) snow.getBlockData();
        if (snowData.getLayers() == snowData.getMinimumLayers()) {
            // Don't disperse bottom layer snow.
            return;
        }

        Block below = snow.getRelative(BlockFace.DOWN);
        Material belowType = below.getType();
        if (belowType == Material.SNOW) {
            org.bukkit.block.data.type.Snow belowData = (org.bukkit.block.data.type.Snow) below.getBlockData();
            if (belowData.getLayers() < belowData.getMaximumLayers()) {
                if (decreaseSnow(snow, false)) {
                    increaseSnow(below, true);
                    return;
                }
            }
        } else if (isReplacable(below) && canLandOn(below.getRelative(BlockFace.DOWN))) {
            if (decreaseSnow(snow, false)) {
                increaseSnow(below, true);
                return;
            }
        }

        if (snowData.getLayers() == snowData.getMaximumLayers() && snow.getRelative(BlockFace.UP).getType() == Material.SNOW) {
            disperse(snow.getRelative(BlockFace.UP));
            return;
        }

        BlockFace best = null;
        int bestDiff = 0;

        List<BlockFace> faces = Arrays.asList(DISPERSE_FACES);
        Collections.shuffle(faces);

        for (BlockFace dir : faces) {
            Block block = snow.getRelative(dir);
            Material blockType = block.getType();

            if (!isReplacable(block) || !canLandOn(block.getRelative(BlockFace.DOWN))) {
                continue;
            }

            int diff;
            if (blockType == Material.SNOW) {
                org.bukkit.block.data.type.Snow blockData = (org.bukkit.block.data.type.Snow) block.getBlockData();
                if(snowData.getLayers() <= blockData.getLayers() + 1) {
                    continue;
                }
                diff = snowData.getLayers() - blockData.getLayers();
            } else {
                diff = blockType.isAir() ? 100 : 99;
            }

            if (diff > bestDiff) {
                best = dir;
                bestDiff = diff;
            }
        }

        if (best != null) {
            Block block = snow.getRelative(best);

            if (decreaseSnow(snow, false)) {
                increaseSnow(block, true);
            }
        }
    }

    private class DispersionQueue implements Runnable {

        @Override
        public void run() {
            int limit = 10000;
            int iterations = Math.min(limit, dispersionQueue.size());
            for (int i = 0; i < iterations; i++) {
                Block block = dispersionQueue.poll();
                if (block == null) {
                    return;
                }
                disperse(block);
            }
        }
    }

    private class SnowRandomTicker implements Runnable {

        @Override
        public void run() {
            for (World world : Bukkit.getWorlds()) {
                boolean meltMode = !world.hasStorm();

                // If we only care about stormy worlds, skip those that aren't storming.
                if (meltMode && !meltSunlight) {
                    continue;
                }

                for (Chunk chunk : world.getLoadedChunks()) {
                    int blockX = (chunk.getX() << 4) + ThreadLocalRandom.current().nextInt(16);
                    int blockZ = (chunk.getZ() << 4) + ThreadLocalRandom.current().nextInt(16);
                    Block block = world.getHighestBlockAt(blockX, blockZ, HeightMap.MOTION_BLOCKING);
                    Material type = block.getType();
                    double temperature = block.getTemperature();

                    Block above = block.getRelative(BlockFace.UP);
                    Material aboveType = above.getType();
                    double aboveTemperature = above.getTemperature();

                    if (type == Material.ICE && aboveType != Material.SNOW && meltMode && temperature > SNOW_MELTING_TEMPERATURE) {
                        block.setType(Material.WATER);
                        continue;
                    }

                    if (aboveType == Material.SNOW) {
                        if (meltMode) {
                            if (meltPartial) {
                                org.bukkit.block.data.type.Snow snowBlock = (org.bukkit.block.data.type.Snow) above.getBlockData();
                                if (aboveTemperature <= SNOW_MELTING_TEMPERATURE && snowBlock.getLayers() == snowBlock.getMinimumLayers() && type != Material.SNOW) {
                                    continue;
                                }
                            }

                            decreaseSnow(above, false);
                        } else if (aboveTemperature <= SNOW_FORM_TEMPERATURE) {
                            increaseSnow(above, true);
                        }
                    }
                }
            }
        }
    }

    private boolean snowPiling;
    private boolean trample;
    private boolean partialTrample;
    private boolean snowballPlacement;
    private boolean slowdown;
    private boolean dispersionMode;
    private boolean pileHigh;
    private int maxPileHeight;
    private boolean jumpTrample;
    private List<BaseBlock> dispersionReplacables;
    private int dispersionTickSpeed;
    private boolean freezeWater;
    private boolean meltSunlight;
    private boolean meltPartial;

    private static List<String> getDefaultReplacables() {
        return Lists.newArrayList(
                BlockTypes.DEAD_BUSH.getId(),
                BlockTypes.GRASS.getId(),
                BlockTypes.FIRE.getId(),
                BlockTypes.FERN.getId());
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("piling", "Enables the piling feature of the Snow mechanic.");
        snowPiling = config.getBoolean("piling", false);

        config.setComment("trample", "Enables the trampling feature of the Snow mechanic.");
        trample = config.getBoolean("trample", false);

        config.setComment("partial-trample-only", "If trampling is enabled, only trample it down to the smallest snow.");
        partialTrample = config.getBoolean("partial-trample-only", false);

        config.setComment("jump-trample", "Require jumping to trample snow.");
        jumpTrample = config.getBoolean("jump-trample", false);

        config.setComment("place-snowball", "Allow snowballs to create snow when they land.");
        snowballPlacement = config.getBoolean("place-snowball", false);

        config.setComment("slowdown", "Slows down entities as they walk through thick snow.");
        slowdown = config.getBoolean("slowdown", false);

        config.setComment("dispersion", "Enable realistic snow dispersion.");
        dispersionMode = config.getBoolean("dispersion", false);

        config.setComment("high-piling", "Allow piling above the 1 block height.");
        pileHigh = config.getBoolean("high-piling", false);

        config.setComment("max-pile-height", "The maximum piling height of high piling snow.");
        maxPileHeight = config.getInt("max-pile-height", 3);

        config.setComment("replaceable-blocks", "A list of blocks that can be replaced by snow dispersion.");
        dispersionReplacables = BlockSyntax.getBlocks(config.getStringList("replaceable-blocks", getDefaultReplacables()), true);

        config.setComment("dispersion-tick-speed", "The speed at which dispersion actions are run");
        dispersionTickSpeed = config.getInt("dispersion-tick-speed", 20);

        config.setComment("freeze-water", "Should snow freeze water?");
        freezeWater = config.getBoolean("freeze-water", false);

        config.setComment("melt-in-sunlight", "Enables snow to melt in sunlight.");
        meltSunlight = config.getBoolean("melt-in-sunlight", false);

        config.setComment("partial-melt-only", "If melt in sunlight is enabled, only melt it down to the smallest snow similar to vanilla MC.");
        meltPartial = config.getBoolean("partial-melt-only", true);
    }
}
