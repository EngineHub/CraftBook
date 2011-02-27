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

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.util.BlockWorldVector2D;

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
            manager.handleBlockRightClick(event);
        }
        
        @Override
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {
            manager.onBlockRedstoneChange(event);
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
