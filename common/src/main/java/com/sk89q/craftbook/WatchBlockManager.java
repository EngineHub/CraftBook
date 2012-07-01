// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.event.block.BlockEvent;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.BlockWorldVector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * Holds the blocks that are watched by mechanics.
 * 
 * @author hash
 */
class WatchBlockManager {
    /**
     * Stores the list of watch blocks.
     */
    private final Map<BlockWorldVector, Set<PersistentMechanic>> watchBlocks;

    /**
     * Construct the object.
     */
    public WatchBlockManager() {
        watchBlocks = new HashMap<BlockWorldVector, Set<PersistentMechanic>>();
    }

    /**
     * Register a mechanic.
     * 
     * @param m
     */
    public void register(PersistentMechanic m) {
        for (BlockWorldVector p : m.getWatchedPositions()) {
            Set<PersistentMechanic> set = watchBlocks.get(p);
            if (set == null) {
                set = new HashSet<PersistentMechanic>(4);
                watchBlocks.put(p, set);
            }
            set.add(m);
        }
    }

    /**
     * Update a mechanic.
     * 
     * @param m
     * @param oldWatchBlocks
     */
    public void update(PersistentMechanic m,
            List<BlockWorldVector> oldWatchBlocks) {

        // This could be more efficient.
        for (BlockWorldVector p : oldWatchBlocks) {
            watchBlocks.get(p).remove(m);
        }

        register(m);
    }

    /**
     * De-registers a mechanic.
     * 
     * @param m
     */
    public void deregister(PersistentMechanic m) {
        for (BlockWorldVector p : m.getWatchedPositions()) {
            if(p!=null && watchBlocks.get(p)!=null && m!=null)
                watchBlocks.get(p).remove(m);
        }
    }

    /**
     * Notify mechanics about a changed block that they are watching.
     * 
     * @param event
     */
    public void notify(BlockEvent event) {
        Set<PersistentMechanic> pms =
                watchBlocks.get(BukkitUtil.toWorldVector(event.getBlock()));

        if (pms == null) {
            return;
        }

        for (PersistentMechanic m : pms) {
            m.onWatchBlockNotification(event);
        }
    }

    /**
     * Get the set of mechanics in a specified chunk.
     * 
     * @param chunk
     * @return the set of mechanics in a specified chunk.
     */
    public Set<PersistentMechanic> getByChunk(BlockWorldVector2D chunk) {
        Set<PersistentMechanic> folks = new HashSet<PersistentMechanic>();
        int chunkX = chunk.getBlockX();
        int chunkZ = chunk.getBlockZ();
        for (Entry<BlockWorldVector, Set<PersistentMechanic>> entry : watchBlocks
                .entrySet()) {
            BlockWorldVector pos = entry.getKey();

            // Different world! Abort
            if (!pos.getWorld().equals(chunk.getWorld()))
                continue;

            int curChunkX = (int) Math.floor(pos.getBlockX() / 16.0);
            int curChunkZ = (int) Math.floor(pos.getBlockZ() / 16.0);
            // Not involved in this chunk!
            if (curChunkX != chunkX || curChunkZ != chunkZ) {
                continue;
            }

            folks.addAll(entry.getValue());
        }
        return folks;
    }
}