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

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.bukkit.BukkitUtil.*;

/**
 * A MechanicManager tracks the BlockVector where loaded Mechanic instances have
 * registered triggerability, and dispatches incoming events by checking for
 * Mechanic instance that might be triggered by the event and by considering
 * instantiation of a new Mechanic instance for unregistered BlockVector.
 * 
 * @author sk89q
 * @author hash
 */
public class MechanicManager {
    /**
     * Logger for errors. The Minecraft namespace is required so that messages
     * are part of Minecraft's root logger.
     */
    protected final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    /**
     * Maps mechanics to their respective world location.
     */
    protected final Map<BlockWorldVector, Mechanic> triggers;
    
    /**
     * List of factories that will be used to detect the mechanic at a location.
     */
    protected final LinkedList<MechanicFactory<Mechanic>> factories;
    
    /**
     * Construct the manager.
     */
    public MechanicManager() {
        triggers = new LinkedHashMap<BlockWorldVector, Mechanic>();
        factories = new LinkedList<MechanicFactory<Mechanic>>();
    }
    
    /**
     * Register a mechanic factory. Make sure that the same factory isn't
     * registered twice -- that condition isn't ever checked.
     * 
     * @param factory
     */
    public void registerMechanic(MechanicFactory<Mechanic> factory) {
        factories.add(factory);
    }
    
    /**
     * Unload all mechanics inside the given chunk.
     * 
     * @param chunk
     */
    public void unload(WorldBlockVector2D chunk) {
        int chunkX = chunk.getBlockX();
        int chunkZ = chunk.getBlockZ();
        
        // We keep track of all the other trigger positions of the mechanics
        // that we are unloading so that we can remove them
        Set<BlockWorldVector> toUnload = new HashSet<BlockWorldVector>();
        
        Iterator<Entry<BlockWorldVector, Mechanic>> it
            = triggers.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<BlockWorldVector, Mechanic> entry = it.next();

            BlockWorldVector pos = entry.getKey();
            Mechanic mechanic = entry.getValue();
            
            // Different world! Abort
            if (!pos.getWorld().equals(chunk.getWorld())) {
                continue;
            }

            int curChunkX = (int)Math.floor(pos.getBlockX() / 16.0);
            int curChunkZ = (int)Math.floor(pos.getBlockZ() / 16.0);
            
            // Not involved in this chunk!
            if (curChunkX != chunkX || curChunkZ != chunkZ) {
                continue;
            }
            
            // We don't want to double unload the mechanic
            if (toUnload.contains(pos)) {
                continue;
            }
            
            try {
                mechanic.unload();
            // Mechanic failed to unload for some reason
            } catch (Throwable t) {
                logger.log(Level.WARNING, "CraftBook mechanic: Failed to unload "
                        + mechanic.getClass().getCanonicalName(), t);
            }
            
            // Now keep track of all the other trigger points
            for (BlockWorldVector otherPos : mechanic.getTriggerPositions()) {
                toUnload.add(otherPos);
            }
            
            it.remove();
        }
        
        // Now let's remove the other points
        for (BlockWorldVector otherPos : toUnload) {
            triggers.remove(otherPos);
        }
    }
    
    /**
     * Attempt to detect a mechanic at a location.
     * 
     * @param pos
     * @return a {@link Mechanic} or null
     */
    protected Mechanic detect(BlockWorldVector pos) {
        for (MechanicFactory<Mechanic> factory : factories) {
            Mechanic mechanic = factory.detect(pos);
            
            if (mechanic != null) {
                return mechanic;
            }
        }
        
        return null;
    }
    
    /**
     * Load a mechanic at a position.
     * 
     * @param pos
     * @return a {@link Mechanic} or null
     */
    protected Mechanic load(BlockWorldVector pos) {
        Mechanic mechanic = triggers.get(pos);
        
        if (mechanic != null) {
            if (mechanic.isActive()) {
                return mechanic;
            } else {
                unload(mechanic);
            }
        }
        
        mechanic = detect(pos);
        
        // No mechanic detected!
        if (mechanic == null) {
            return null;
        }

        // Register mechanic trigger positions
        for (BlockWorldVector otherPos : mechanic.getTriggerPositions()) {
            triggers.put(otherPos, mechanic);
        }
        
        return mechanic;
    }
    
    /**
     * Unload a mechanic. This will also remove the trigger points from
     * this mechanic manager.
     * 
     * @param mechanic
     */
    protected void unload(Mechanic mechanic) {
        try {
            mechanic.unload();
        // Mechanic failed to unload for some reason
        } catch (Throwable t) {
            logger.log(Level.WARNING, "CraftBook mechanic: Failed to unload "
                    + mechanic.getClass().getCanonicalName(), t);
        }
        
        for (BlockWorldVector otherPos : mechanic.getTriggerPositions()) {
            triggers.remove(otherPos);
        }
    }
    
    /**
     * Handle a block right click event.
     * 
     * @param event
     * @return true if there was a mechanic to process the event
     */
    public boolean handleBlockRightClick(BlockRightClickEvent event) {
        if (!passesFilter(event)) {
            return false;
        }
        
        BlockWorldVector pos = toWorldVector(event.getBlock());
        Mechanic mechanic = load(pos);
        if (mechanic != null) {
            mechanic.onRightClick(event);
            return true;
        }
        
        return false;
    }
    
    /**
     * Used to filter events for processing. This allows for short circuiting
     * code so that code isn't checked unnecessarily.
     * 
     * @param event
     * @return
     */
    protected boolean passesFilter(BlockEvent event) {
        return true;
    }
}
