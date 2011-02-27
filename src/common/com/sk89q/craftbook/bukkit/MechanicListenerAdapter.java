// $Id$
/*
 * CraftBook
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.bukkit;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.BlockWorldVector2D;
import com.sk89q.craftbook.util.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 * 
 * @author sk89q
 */
public class MechanicListenerAdapter {
    
    /**
     * Holds the plugin that events are registered through.
     */
    protected JavaPlugin plugin;
    
    /**
     * Constructs the adapter.
     * 
     * @param plugin
     */
    public MechanicListenerAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Register events.
     * 
     * @param manager
     */
    public void register(MechanicManager manager) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        BlockListener blockListener = new MechanicBlockListener(manager);
        WorldListener worldListener = new MechanicWorldListener(manager);

        pluginManager.registerEvent(Type.BLOCK_RIGHTCLICKED, blockListener,
                Priority.Normal, plugin);
        pluginManager.registerEvent(Type.REDSTONE_CHANGE, blockListener,
                Priority.Normal, plugin);
        pluginManager.registerEvent(Type.CHUNK_UNLOADED, worldListener,
                Priority.Normal, plugin);
    }
    
    /**
     * Block listener for processing block events.
     * 
     * @author sk89q
     */
    protected static class MechanicBlockListener extends BlockListener {
        
        protected MechanicManager manager;
        
        /**
         * Construct the listener.
         * 
         * @param manager
         */
        public MechanicBlockListener(MechanicManager manager) {
            this.manager = manager;
        }
        
        @Override
        public void onBlockRightClick(BlockRightClickEvent event) {
            manager.dispatchBlockRightClick(event);
        }
        
        @Override
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {
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
            if (!wasChange) {
                return;
            }

            int x = v.getBlockX();
            int y = v.getBlockY();
            int z = v.getBlockZ();
            
            int type = block.getTypeId();

            // When this hook has been called, the level in the world has not
            // yet been updated, so we're going to do this very ugly thing of
            // faking the value with the new one whenever the data value of this
            // block is requested -- it is quite ugly
            // TODO: Fake data is for ICs
            try {
                if (type == BlockID.LEVER) {
                    // Fake data
                    /*w.fakeData(x, y, z,
                        newLevel > 0
                            ? w.getData(x, y, z) | 0x8
                            : w.getData(x, y, z) & 0x7);*/
                } else if (type == BlockID.STONE_PRESSURE_PLATE) {
                    // Fake data
                    /*w.fakeData(x, y, z,
                        newLevel > 0
                            ? w.getData(x, y, z) | 0x1
                            : w.getData(x, y, z) & 0x14);*/
                } else if (type == BlockID.WOODEN_PRESSURE_PLATE) {
                    // Fake data
                    /*w.fakeData(x, y, z,
                        newLevel > 0
                            ? w.getData(x, y, z) | 0x1
                            : w.getData(x, y, z) & 0x14);*/
                } else if (type == BlockID.STONE_BUTTON) {
                    // Fake data
                    /*w.fakeData(x, y, z,
                        newLevel > 0
                            ? w.getData(x, y, z) | 0x8
                            : w.getData(x, y, z) & 0x7);*/
                } else if (type == BlockID.REDSTONE_WIRE) {
                    // Fake data
                    //w.fakeData(x, y, z, newLevel);

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
                    if (!BlockType.isRedstoneBlock(westSide)
                            && !BlockType.isRedstoneBlock(eastSide)
                            && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0)
                            && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0)
                            && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                            && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                        // Possible blocks north / south
                        handleDirectWireInput(new WorldVector(world, x - 1, y, z), isOn, v, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(world, x + 1, y, z), isOn, v, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(world, x - 1, y - 1, z), isOn, v, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(world, x + 1, y - 1, z), isOn, v, oldLevel, newLevel);
                    }

                    if (!BlockType.isRedstoneBlock(northSide)
                            && !BlockType.isRedstoneBlock(southSide)
                            && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0)
                            && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0)
                            && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                            && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                        // Possible blocks west / east
                        handleDirectWireInput(new WorldVector(world, x, y, z - 1), isOn, v, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(world, x, y, z + 1), isOn, v, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(world, x, y - 1, z - 1), isOn, v, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(world, x, y - 1, z + 1), isOn, v, oldLevel, newLevel);
                    }

                    // Can be triggered from below
                    handleDirectWireInput(new WorldVector(world, x, y + 1, z), isOn, v, oldLevel, newLevel);

                    return;
                }

                // For redstone wires, the code already exited this method
                // Non-wire blocks proceed

                handleDirectWireInput(new WorldVector(world, x - 1, y, z), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x + 1, y, z), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x - 1, y - 1, z), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x + 1, y - 1, z), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x, y, z - 1), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x, y, z + 1), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x, y - 1, z - 1), isOn, v, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(world, x, y - 1, z + 1), isOn, v, oldLevel, newLevel);

                // Can be triggered from below
                handleDirectWireInput(new WorldVector(world, x, y + 1, z), isOn, v, oldLevel, newLevel);

                return;
            } finally {
                //w.destroyFake();
            }
        }
        
        /**
         * Handle the direct wire input.
         * 
         * @param pt
         * @param isOn
         * @param v
         * @param oldLevel
         * @param newLevel
         */
        protected void handleDirectWireInput(WorldVector pt,
                boolean isOn, WorldVector v, int oldLevel, int newLevel) {
            Block block = pt.getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            manager.dispatchBlockRedstoneChange(
                    new BlockRedstoneEvent(block, oldLevel, newLevel));
        }
    }
    
    /**
     * Block listener for processing block events.
     * 
     * @author sk89q
     */
    protected static class MechanicWorldListener extends WorldListener {
        
        protected MechanicManager manager;
        
        /**
         * Construct the listener.
         * 
         * @param manager
         */
        public MechanicWorldListener(MechanicManager manager) {
            this.manager = manager;
        }

        /**
         * Called when a chunk is unloade.d
         */
        @Override
        public void onChunkUnloaded(ChunkUnloadEvent event) {
            int chunkX = event.getChunk().getX();
            int chunkZ = event.getChunk().getZ();
            
            manager.unload(new BlockWorldVector2D(event.getWorld(), chunkX, chunkZ));
        }
    }
}
