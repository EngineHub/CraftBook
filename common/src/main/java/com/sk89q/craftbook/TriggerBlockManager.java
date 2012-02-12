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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.BlockWorldVector2D;

/**
 * This keeps track of trigger blocks. Trigger blocks are what triggers
 * a mechanic (i.e. a [Gate] sign).
 * 
 * @author hash
 */
class TriggerBlockManager {

    /**
     * Holds the list of triggers.
     */
    private final Map<BlockWorldVector, PersistentMechanic> triggers;
    
    /**
     * Construct the manager.
     */
    public TriggerBlockManager() {
        triggers = new HashMap<BlockWorldVector, PersistentMechanic>();
    }

    /**
     * Register a mechanic with the manager.
     * 
     * @param m
     */
    public void register(PersistentMechanic m) {
        // Debugging code
        if (MechanicManager.DEBUG) {
            for (BlockWorldVector p : m.getTriggerPositions()) {
                if (triggers.get(p) != null) {
                    throw new CraftbookRuntimeException(new IllegalStateException(
                            p + " has already been claimed by another Mechanic"));
                }
            }
        }
        
        for (BlockWorldVector p : m.getTriggerPositions()) {
            triggers.put(p, m);
        }
    }

    /**
     * Dereigster a mechanic.
     * 
     * @param m
     */
    public void deregister(PersistentMechanic m) {
        // Debugging code
        if (MechanicManager.DEBUG) {
            for (BlockWorldVector p : m.getTriggerPositions()) {
                if (triggers.get(p) != m) {
                    throw new CraftbookRuntimeException(new IllegalStateException(
                            p + " was occupied by another Mechanic"));
                }
            }
        }
    
        for (BlockWorldVector p : m.getTriggerPositions()) {
            triggers.put(p, null);
        }
    }

    /**
     * Get the persistent mechanic associated with a particular position.
     * 
     * @param p
     * @return a persistent mechanic if one is triggered by the location; null
     *         if one does not already exist (detection for a potential mechanic
     *         that should exist is not performed).
     */
    public PersistentMechanic get(BlockWorldVector p) {
        return triggers.get(p);
    }

    /**
     * Get a list of mechanics that in a specified chunk.
     * 
     * Implemented by performing a walk over the entire list of loaded
     * PersistentMechanic in the universe.
     * 
     * @param chunk
     * @return a set including every PersistentMechanic with at least one
     *         trigger in the given chunk. (PersistentMechanic with watched
     *         blocks in the chunk are not included.)
     */
    public Set<PersistentMechanic> getByChunk(BlockWorldVector2D chunk) {
        Set<PersistentMechanic> folks = new HashSet<PersistentMechanic>();
        int chunkX = chunk.getBlockX();
        int chunkZ = chunk.getBlockZ();
        Iterator<Entry<BlockWorldVector, PersistentMechanic>> it = triggers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockWorldVector, PersistentMechanic> entry = it.next();
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
            
            PersistentMechanic pMechanic = entry.getValue();
            
            if (pMechanic != null)
                folks.add(entry.getValue());
        }
        return folks;
    }
}