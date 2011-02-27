// Id
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
import org.bukkit.event.block.*;
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
     * List of factories that will be used to detect mechanisms at a location.
     */
    protected final LinkedList<MechanicFactory<? extends Mechanic>> factories;
    
    private final TriggerKeeper triggerman;
    
    private final DefinerKeeper definerman;
    
    
    
    //
    //  Setup
    //
    
    /**
     * Construct the manager.
     */
    public MechanicManager() {
        factories = new LinkedList<MechanicFactory<? extends Mechanic>>();
        triggerman = new TriggerKeeper();
        definerman = new DefinerKeeper();
    }
    
    /**
     * Register a mechanic factory. Make sure that the same factory isn't
     * registered twice -- that condition isn't ever checked.
     * 
     * @param factory
     */
    public void register(MechanicFactory<? extends Mechanic> factory) {
        factories.add(factory);
    }
    
    
    
    
    
    //
    //  Event handling
    //
    
    /**
     * Handle a block right click event.
     * 
     * @param event
     * @return true if there was a mechanic to process the event
     */
    public boolean handleBlockRightClick(BlockRightClickEvent event) {
        if (!passesFilter(event))
            return false;
        
        // annouce the event to anyone who considers it to be on one of their defining blocks
        definerman.notify(event);
        
        // see if this event could be occuring on any mechanism's triggering blocks.
        BlockWorldVector pos = toWorldVector(event.getBlock());
        try {
            Mechanic mechanic = load(pos);
            if (mechanic != null) {
                mechanic.onRightClick(event);
                return true;
            }
        } catch (InvalidMechanismException $e) {
            //FIXME tell the player about it.
        }
        
        return false;
    }
    
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (!passesFilter(event))
            return;
        
        for (BlockRedstoneEvent kitten : BlockRedstoneNeighborEvent.haveKittens(event)) { 
            BlockWorldVector pos = toWorldVector(event.getBlock());
            try {
                Mechanic mechanic = load(pos);
                if (mechanic != null)
                    mechanic.onBlockRedstoneChange(event);
            } catch (InvalidMechanismException $e) {
                //FIXME tell the... erm, sombody... about it.
            }
        }
    }
    
    
    
    
    

    /**
     * Load a Mechanic at a position. May return an already existing
     * PersistentMechanic if one is triggered at that position, or return a new
     * Mechanic (persistent or otherwise; if the new mechanic is persistent, it
     * will have already been registered with this manager).
     * 
     * @param pos
     * @return a {@link Mechanic} if a mechanism could be found at the location;
     *         null otherwise
     * @throws InvalidMechanismException
     *             if it appears that the position is intended to me a
     *             mechanism, but the mechanism is misconfigured and inoperable.
     */
    protected Mechanic load(BlockWorldVector pos) throws InvalidMechanismException {
        Mechanic mechanic = triggerman.get(pos);
        
        if (mechanic != null) {
            if (mechanic.isActive()) {
                return mechanic;
            } else {
                unload(mechanic);
            }
        }
        
        mechanic = detect(pos);
        
        // No mechanic detected!
        if (mechanic == null)
            return null;
        
        // Register mechanic if it's a persistent type 
        if (mechanic instanceof PersistentMechanic) {
            PersistentMechanic pm = (PersistentMechanic) mechanic;
            triggerman.registerMechanic(pm);
            definerman.registerMechanic(pm);
        }
        
        return mechanic;
    }
    
    /**
     * Attempt to detect a mechanic at a location.
     * 
     * @param pos
     * @return a {@link Mechanic} if a mechanism could be found at the location;
     *         null otherwise
     * @throws InvalidMechanismException
     *             if it appears that the position is intended to me a
     *             mechanism, but the mechanism is misconfigured and inoperable.
     */
    protected Mechanic detect(BlockWorldVector pos) throws InvalidMechanismException {
        Mechanic mechanic = null;
        for (MechanicFactory<? extends Mechanic> factory : factories)
            if ((mechanic = factory.detect(pos)) != null) break;
        return mechanic;
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
    
    
    
    
    
    //
    //  Logic for dealing with PersistentMechanic instances.
    //
    
    /**
     * Unload all mechanics inside the given chunk.
     * 
     * @param chunk
     */
    public void unload(BlockWorldVector2D chunk) {
        Set<PersistentMechanic> folks = triggerman.getByChunk(chunk);
        folks.addAll(definerman.getByChunk(chunk));
        for (Mechanic m : folks)
            unload(m);
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
        } catch (Throwable t) { // Mechanic failed to unload for some reason
            logger.log(Level.WARNING, "CraftBook mechanic: Failed to unload "
                    + mechanic.getClass().getCanonicalName(), t);
        }
        
        if (mechanic instanceof PersistentMechanic) {
            PersistentMechanic pm = (PersistentMechanic)mechanic;
            triggerman.deregisterMechanic(pm);
            definerman.deregisterMechanic(pm);
        }
    }
    
    
    
    //
    //  These are helper classes for tracking the state that 
    //    dispatching to PersistentMechanic requires.
    //
    
    private static final boolean CHECK_SANITY = true;
    
    private class TriggerKeeper {
        public TriggerKeeper() {
            triggers = new HashMap<BlockWorldVector, PersistentMechanic>();
        }
        
        private final Map<BlockWorldVector, PersistentMechanic> triggers;
        
        public void registerMechanic(PersistentMechanic m) {
            if (CHECK_SANITY) 
                for (BlockWorldVector p : m.getTriggerPositions())
                    if (triggers.get(p) != null) throw new CraftbookRuntimeException(new IllegalStateException("Position "+p+" has already been claimed by another Mechanic; registration not performed."));
            for (BlockWorldVector p : m.getTriggerPositions())
                triggers.put(p, m);
        }
        
        public void deregisterMechanic(PersistentMechanic m) {
            if (CHECK_SANITY) 
                for (BlockWorldVector p : m.getTriggerPositions())
                    if (triggers.get(p) != m) throw new CraftbookRuntimeException(new IllegalStateException("Position "+p+" has occupied by another Mechanic; deregistration not performed."));
            for (BlockWorldVector p : m.getTriggerPositions())
                triggers.put(p, null);
        }
        
        public PersistentMechanic get(BlockWorldVector p) {
            return triggers.get(p);
        }
        
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
                
                int curChunkX = (int)Math.floor(pos.getBlockX() / 16.0);
                int curChunkZ = (int)Math.floor(pos.getBlockZ() / 16.0);
                // Not involved in this chunk!
                if (curChunkX != chunkX || curChunkZ != chunkZ) {
                    continue;
                }
                
                folks.add(entry.getValue());
            }
            return folks;
        }
    }
    
    private class DefinerKeeper {
        public DefinerKeeper() {
            definers = new HashMap<BlockWorldVector, Set<PersistentMechanic>>();
        }
        
        private final Map<BlockWorldVector, Set<PersistentMechanic>> definers;
        
        public void registerMechanic(PersistentMechanic m) {
            for (BlockWorldVector p : m.getDefiningPositions()) {
                Set<PersistentMechanic> set = definers.get(p);
                if (set == null) {
                    set = new HashSet<PersistentMechanic>(4);
                    definers.put(p, set);
                }
                set.add(m);
            }
        }
        
        public void updateMechanic(PersistentMechanic m, List<BlockWorldVector> oldDefiners) {
            // this could be more efficient.
            for (BlockWorldVector p : oldDefiners)
                definers.get(p).remove(m);
            registerMechanic(m);
        }
        
        public void deregisterMechanic(PersistentMechanic m) {
            for (BlockWorldVector p : m.getDefiningPositions())
                definers.get(p).remove(m);
        }
        
        public void notify(BlockRightClickEvent event) {
            Set<PersistentMechanic> pms = definers.get(toWorldVector(event.getBlock()));
            if (pms == null) return;
            for (PersistentMechanic m : pms)
                m.onRightClick(event);
        }
        
        public Set<PersistentMechanic> getByChunk(BlockWorldVector2D chunk) {
            Set<PersistentMechanic> folks = new HashSet<PersistentMechanic>();
            int chunkX = chunk.getBlockX();
            int chunkZ = chunk.getBlockZ();
            for (Entry<BlockWorldVector, Set<PersistentMechanic>> entry : definers.entrySet()) {
                BlockWorldVector pos = entry.getKey();
                
                // Different world! Abort
                if (!pos.getWorld().equals(chunk.getWorld()))
                    continue;
                
                int curChunkX = (int)Math.floor(pos.getBlockX() / 16.0);
                int curChunkZ = (int)Math.floor(pos.getBlockZ() / 16.0);
                // Not involved in this chunk!
                if (curChunkX != chunkX || curChunkZ != chunkZ) {
                    continue;
                }
                
                folks.addAll(entry.getValue());
            }
            return folks;
        }
    }
    
}
