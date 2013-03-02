// $Id$
/*
 * CraftBook Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.material.Diode;

import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.BlockWorldVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 *
 * @author sk89q
 */
public class MechanicListenerAdapter {

    private List<MechanicManager> managerList = new ArrayList<MechanicManager>();

    MechanicPlayerListener playerListener = new MechanicPlayerListener();
    MechanicBlockListener blockListener = new MechanicBlockListener();
    MechanicWorldListener worldListener = new MechanicWorldListener();

    public static ArrayList<Event> ignoredEvents = new ArrayList<Event>();

    /**
     * Constructs the adapter.
     */
    public MechanicListenerAdapter() {

        CraftBookPlugin.registerEvents(playerListener);
        CraftBookPlugin.registerEvents(blockListener);
        CraftBookPlugin.registerEvents(worldListener);
    }

    public List<MechanicManager> getManagers() {

        return managerList;
    }

    /**
     * Register events.
     *
     * @param manager
     */
    public void register(MechanicManager manager) {

        managerList.add(manager);

        playerListener.addManager(manager);
        blockListener.addManager(manager);
        worldListener.addManager(manager);
    }

    public void register(MechanicManager manager, boolean player, boolean block, boolean world, boolean vehicle) {

        managerList.add(manager);

        if (player) playerListener.addManager(manager);
        if (block) blockListener.addManager(manager);
        if (world) worldListener.addManager(manager);
        //TODO if (vehicle)
    }

    /**
     * Player listener for detecting interactions with mechanic triggers.
     *
     * @author hash
     */
    protected static class MechanicPlayerListener implements Listener {

        protected static final List<MechanicManager> managers = new ArrayList<MechanicManager>();

        public void addManager(MechanicManager manager) {

            managers.add(manager);
        }

        /**
         * Construct the listener.
         *
         * @param manager
         */
        public MechanicPlayerListener(MechanicManager... manager) {

            managers.addAll(Arrays.asList(manager));
        }

        public MechanicPlayerListener() {

        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerInteract(PlayerInteractEvent event) {

            if (ignoredEvents.contains(event)) {
                ignoredEvents.remove(event);
                return;
            }
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                for (MechanicManager manager : managers) {
                    manager.dispatchBlockRightClick(event);
                }
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                for (MechanicManager manager : managers) {
                    manager.dispatchBlockLeftClick(event);
                }
            }
        }
    }

    /**
     * Block listener for processing block events.
     *
     * @author sk89q
     */
    protected static class MechanicBlockListener implements Listener {

        protected static final List<MechanicManager> managers = new ArrayList<MechanicManager>();

        public void addManager(MechanicManager manager) {

            managers.add(manager);
        }

        /**
         * Construct the listener.
         *
         * @param manager
         */
        public MechanicBlockListener(MechanicManager... manager) {

            managers.addAll(Arrays.asList(manager));
        }

        /**
         * Construct the listener.
         */
        public MechanicBlockListener() {

        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onSignChange(SignChangeEvent event) {

            if (ignoredEvents.contains(event)) {
                ignoredEvents.remove(event);
                return;
            }
            for (MechanicManager manager : managers) {
                manager.dispatchSignChange(event);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockBreak(BlockBreakEvent event) {

            if (ignoredEvents.contains(event)) {
                ignoredEvents.remove(event);
                return;
            }
            for (MechanicManager manager : managers) {
                manager.dispatchBlockBreak(event);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {

            if (ignoredEvents.contains(event)) {
                ignoredEvents.remove(event);
                return;
            }
            int oldLevel = event.getOldCurrent();
            int newLevel = event.getNewCurrent();
            Block block = event.getBlock();
            World world = block.getWorld();
            BlockWorldVector v = BukkitUtil.toWorldVector(block);

            // Give the method a BlockVector instead of a Block
            boolean wasOn = oldLevel >= 1;
            boolean isOn = newLevel >= 1;
            boolean wasChange = wasOn != isOn;

            // For efficiency reasons, we're only going to consider changes between
            // off and on state, and ignore simple current changes (i.e. 15->13)
            if (!wasChange) return;

            LocalWorld w = BukkitUtil.getLocalWorld(world);
            int x = v.getBlockX();
            int y = v.getBlockY();
            int z = v.getBlockZ();

            int type = block.getTypeId();

            // When this hook has been called, the level in the world has not
            // yet been updated, so we're going to do this very ugly thing of
            // faking the value with the new one whenever the data value of this
            // block is requested -- it is quite ugly

            if (type == BlockID.REDSTONE_WIRE) {

                if (CraftBookPlugin.inst().getConfiguration().indirectRedstone){

                    // power all blocks around the redstone wire on the same y level
                    // north/south
                    handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
                    // east/west
                    handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);

                    // Can be triggered from below
                    handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);

                    // Can be triggered from above (Eg, glass->glowstone like redstone lamps)
                    handleDirectWireInput(new WorldVector(w, x, y - 1, z), block, oldLevel, newLevel);
                } else {

                    int above = world.getBlockTypeIdAt(x, y + 1, z);

                    int westSide = world.getBlockTypeIdAt(x, y, z + 1);
                    int westSideAbove = world.getBlockTypeIdAt(x, y + 1, z + 1);
                    int westSideBelow = world.getBlockTypeIdAt(x, y - 1, z + 1);
                    int eastSide = world.getBlockTypeIdAt(x, y, z - 1);
                    int eastSideAbove = world.getBlockTypeIdAt(x, y + 1, z - 1);
                    int eastSideBelow = world.getBlockTypeIdAt(x, y - 1, z - 1);

                    int northSide = world.getBlockTypeIdAt(x - 1, y, z);
                    int northSideAbove = world.getBlockTypeIdAt(x - 1, y + 1, z);
                    int northSideBelow = world.getBlockTypeIdAt(x - 1, y - 1, z);
                    int southSide = world.getBlockTypeIdAt(x + 1, y, z);
                    int southSideAbove = world.getBlockTypeIdAt(x + 1, y + 1, z);
                    int southSideBelow = world.getBlockTypeIdAt(x + 1, y - 1, z);

                    // Make sure that the wire points to only this block
                    if (!BlockType.isRedstoneBlock(westSide) && !BlockType.isRedstoneBlock(eastSide)
                            && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                            && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                        // Possible blocks north / south
                        handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), block, oldLevel, newLevel);
                    }

                    if (!BlockType.isRedstoneBlock(northSide) && !BlockType.isRedstoneBlock(southSide)
                            && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                            && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                        // Possible blocks west / east
                        handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), block, oldLevel, newLevel);
                    }

                    // Can be triggered from below
                    handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);
                }
                return;
            } else if (type == BlockID.REDSTONE_REPEATER_OFF || type == BlockID.REDSTONE_REPEATER_ON) {

                Diode diode = (Diode) block.getState().getData();
                BlockFace f = diode.getFacing();
                handleDirectWireInput(new WorldVector(w, x + f.getModX(), y, z + f.getModZ()), block, oldLevel, newLevel);
                if(block.getRelative(f).getTypeId() != 0)
                    handleDirectWireInput(new WorldVector(w, x + f.getModX(), y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                return;
            }
            // For redstone wires and repeaters, the code already exited this method
            // Non-wire blocks proceed

            handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), block, oldLevel, newLevel);
            handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), block, oldLevel, newLevel);

            // Can be triggered from below
            handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);
        }

        /**
         * Handle the direct wire input.
         *
         * @param pt
         * @param sourceBlock
         * @param oldLevel
         * @param newLevel
         */
        protected void handleDirectWireInput(WorldVector pt, Block sourceBlock, int oldLevel, int newLevel) {

            Block block = ((BukkitWorld) pt.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            for (MechanicManager manager : managers) {
                manager.dispatchBlockRedstoneChange(new SourcedBlockRedstoneEvent(sourceBlock, block, oldLevel, newLevel));
            }
        }
    }

    /**
     * World listener for processing world events.
     *
     * @author sk89q
     */
    protected class MechanicWorldListener implements Listener {

        protected final List<MechanicManager> managers = new ArrayList<MechanicManager>();

        public void addManager(MechanicManager manager) {

            managers.add(manager);
        }

        /**
         * Construct the listener.
         *
         * @param manager
         */
        public MechanicWorldListener(MechanicManager... manager) {

            managers.addAll(Arrays.asList(manager));
        }

        public MechanicWorldListener() {

        }

        /**
         * Called when a chunk is loaded.
         */
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChunkLoad(final ChunkLoadEvent event) {

            if (ignoredEvents.contains(event)) {
                ignoredEvents.remove(event);
                return;
            }
            CraftBookPlugin.server().getScheduler().scheduleSyncDelayedTask(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run() {

                    for (MechanicManager manager : managers) { manager.enumerate(event.getChunk()); }
                }
            }, 2);
        }

        /**
         * Called when a chunk is unloaded.
         */
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChunkUnload(ChunkUnloadEvent event) {

            if (ignoredEvents.contains(event)) {
                ignoredEvents.remove(event);
                return;
            }
            int chunkX = event.getChunk().getX();
            int chunkZ = event.getChunk().getZ();

            for (MechanicManager manager : managers) {
                manager.unload(new BlockWorldVector2D(BukkitUtil.getLocalWorld(event.getWorld()), chunkX, chunkZ),
                        event);
            }
        }
    }
}