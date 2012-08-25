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

import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.RedstoneUtil;
import com.sk89q.worldedit.BlockWorldVector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginManager;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 *
 * @author sk89q
 */
public class MechanicListenerAdapter {

    /**
     * Holds the plugin that events are registered through.
     */
    protected final BaseBukkitPlugin plugin;

    /**
     * Constructs the adapter.
     *
     * @param plugin
     */
    public MechanicListenerAdapter(BaseBukkitPlugin plugin) {

        this.plugin = plugin;
    }

    /**
     * Register events.
     *
     * @param manager
     */
    public void register(MechanicManager manager) {

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Listener playerListener = new MechanicPlayerListener(manager, plugin);
        Listener blockListener = new MechanicBlockListener(manager, plugin);
        Listener worldListener = new MechanicWorldListener(manager, plugin);

        pluginManager.registerEvents(playerListener, plugin);
        pluginManager.registerEvents(blockListener, plugin);
        pluginManager.registerEvents(worldListener, plugin);
    }

    /**
     * Player listener for detecting interactions with mechanic triggers.
     *
     * @author hash
     */
    protected static class MechanicPlayerListener implements Listener {

        protected final MechanicManager manager;
        protected final BaseBukkitPlugin plugin;

        /**
         * Construct the listener.
         *
         * @param manager
         * @param plugin
         */
        public MechanicPlayerListener(MechanicManager manager, BaseBukkitPlugin plugin) {

            this.manager = manager;
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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

        protected final MechanicManager manager;
        protected final BaseBukkitPlugin plugin;

        /**
         * Construct the listener.
         *
         * @param manager
         * @param plugin
         */
        public MechanicBlockListener(MechanicManager manager, BaseBukkitPlugin plugin) {

            this.manager = manager;
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSignChange(SignChangeEvent event) {

            manager.dispatchSignChange(event);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent event) {

            manager.dispatchBlockBreak(event);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockRedstoneChange(BlockPhysicsEvent event) {

            if (!RedstoneUtil.isPotentialPowerSource(event.getChangedTypeId())) return;

            manager.dispatchBlockRedstoneChange(event);
        }
    }

    /**
     * Block listener for processing block events.
     *
     * @author sk89q
     */
    protected class MechanicWorldListener implements Listener {

        protected final MechanicManager manager;
        protected final BaseBukkitPlugin plugin;

        /**
         * Construct the listener.
         *
         * @param manager
         */
        public MechanicWorldListener(MechanicManager manager, BaseBukkitPlugin plugin) {

            this.manager = manager;
            this.plugin = plugin;
        }

        /**
         * Called when a chunk is loaded.
         */
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onChunkUnload(ChunkUnloadEvent event) {

            int chunkX = event.getChunk().getX();
            int chunkZ = event.getChunk().getZ();

            manager.unload(new BlockWorldVector2D(BukkitUtil.getLocalWorld(event.getWorld()), chunkX, chunkZ), event);
        }
    }
}
