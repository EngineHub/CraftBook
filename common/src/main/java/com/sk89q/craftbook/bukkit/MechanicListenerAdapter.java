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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
        Listener playerListener = new MechanicPlayerListener(manager);
        Listener blockListener = new MechanicBlockListener(manager);
        Listener worldListener = new MechanicWorldListener(manager);

        pluginManager.registerEvents(playerListener, plugin);
        pluginManager.registerEvents(blockListener, plugin);
        pluginManager.registerEvents(worldListener, plugin);
    }
    
    /**
     * Player listener for detecting interactions with mechanic triggers.
     * 
     * @author hash
     * 
     */
    protected static class MechanicPlayerListener implements Listener {
        protected MechanicManager manager;
        
        /**
         * Construct the listener.
         * 
         * @param manager
         */
        public MechanicPlayerListener(MechanicManager manager) {
            this.manager = manager;
        }
        
        @EventHandler(ignoreCancelled = false)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                manager.dispatchBlockRightClick(event);
            }
            
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                manager.dispatchBlockLeftClick(event);
            }
        }
    }
    
    /**
     * Block listener for processing block events.
     * 
     * @author sk89q
     */
    protected static class MechanicBlockListener implements Listener {
        
        protected MechanicManager manager;
        
        /**
         * Construct the listener.
         * 
         * @param manager
         */
        public MechanicBlockListener(MechanicManager manager) {
            this.manager = manager;
        }

        @EventHandler
        public void onSignChange(SignChangeEvent event) {
            manager.dispatchSignChange(event);
        }
        
        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
        	manager.dispatchBlockBreak(event);
        }
        
        @EventHandler
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
            
            LocalWorld w = BukkitUtil.getLocalWorld(world);
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

                    int above =          world.getBlockTypeIdAt(x,     y + 1, z);
                    
                    int westSide =       world.getBlockTypeIdAt(x,     y,     z + 1);
                    int westSideAbove =  world.getBlockTypeIdAt(x,     y + 1, z + 1);
                    int westSideBelow =  world.getBlockTypeIdAt(x,     y - 1, z + 1);
                    int eastSide =       world.getBlockTypeIdAt(x,     y,     z - 1);
                    int eastSideAbove =  world.getBlockTypeIdAt(x,     y + 1, z - 1);
                    int eastSideBelow =  world.getBlockTypeIdAt(x,     y - 1, z - 1);
                    
                    int northSide =      world.getBlockTypeIdAt(x - 1, y,     z);
                    int northSideAbove = world.getBlockTypeIdAt(x - 1, y + 1, z);
                    int northSideBelow = world.getBlockTypeIdAt(x - 1, y - 1, z);
                    int southSide =      world.getBlockTypeIdAt(x + 1, y,     z);
                    int southSideAbove = world.getBlockTypeIdAt(x + 1, y + 1, z);
                    int southSideBelow = world.getBlockTypeIdAt(x + 1, y - 1, z);
                    
                    
                    // Make sure that the wire points to only this block
                    if (!BlockType.isRedstoneBlock(westSide)
                            && !BlockType.isRedstoneBlock(eastSide)
                            && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                            && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                        // Possible blocks north / south
                        handleDirectWireInput(new WorldVector(w, x - 1, y, z), isOn, block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x + 1, y, z), isOn, block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), isOn, block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), isOn, block, oldLevel, newLevel);
                    }

                    if (!BlockType.isRedstoneBlock(northSide)
                            && !BlockType.isRedstoneBlock(southSide)
                            && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                            && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                        // Possible blocks west / east
                        handleDirectWireInput(new WorldVector(w, x, y, z - 1), isOn, block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y, z + 1), isOn, block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), isOn, block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), isOn, block, oldLevel, newLevel);
                    }

                    // Can be triggered from below
                    handleDirectWireInput(new WorldVector(w, x, y + 1, z), isOn, block, oldLevel, newLevel);

                    return;
                }

                // For redstone wires, the code already exited this method
                // Non-wire blocks proceed

                handleDirectWireInput(new WorldVector(w, x - 1, y, z), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + 1, y, z), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x, y, z - 1), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x, y, z + 1), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), isOn, block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), isOn, block, oldLevel, newLevel);

                // Can be triggered from below
                handleDirectWireInput(new WorldVector(w, x, y + 1, z), isOn, block, oldLevel, newLevel);

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
         * @param sourceBlock
         * @param oldLevel
         * @param newLevel
         */
        protected void handleDirectWireInput(WorldVector pt,
                boolean isOn, Block sourceBlock, int oldLevel, int newLevel) {
            Block block = ((BukkitWorld)pt.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            manager.dispatchBlockRedstoneChange(
                    new SourcedBlockRedstoneEvent(sourceBlock, block, oldLevel, newLevel));
        }
    }
    
    /**
     * Block listener for processing block events.
     * 
     * @author sk89q
     */
    protected class MechanicWorldListener implements Listener {
        
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
         * Called when a chunk is loaded.
         */
        @EventHandler
        public void onChunkLoad(final ChunkLoadEvent event) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    manager.enumerate(event.getChunk());
                }
            }, 2);
        }

        /**
         * Called when a chunk is unloaded.
         */
        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            int chunkX = event.getChunk().getX();
            int chunkZ = event.getChunk().getZ();
            
            manager.unload(new BlockWorldVector2D(BukkitUtil.getLocalWorld(event.getWorld()), chunkX, chunkZ));
        }
    }
}
