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
    public static final boolean DEBUG = false;
    
    /**
     * Logger for errors. The Minecraft namespace is required so that messages
     * are part of Minecraft's root logger.
     */
    protected final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    /**
     * List of factories that will be used to detect mechanisms at a location.
     */
    protected final LinkedList<MechanicFactory<? extends Mechanic>> factories;

    /**
     * Keeps track of trigger blocks. Trigger blocks are the blocks that
     * will activate mechanics. No block can be a trigger block for two
     * mechanics at once. An example of a trigger block is a sign block
     * with [Gate] as the second line, triggering the gate mechanic.
     */
    private final TriggerBlockManager triggersManager;

    /**
     * Keeps track of watch blocks. A persistent mechanic have several watch
     * blocks that entail the blocks that the mechanic may use. These blocks
     * may not be trigger blocks and are probably not. Watch blocks aren't
     * utilized yet.
     */
    private final WatchBlockManager watchBlockManager;

    /**
     * Construct the manager.
     */
    public MechanicManager() {
        factories = new LinkedList<MechanicFactory<? extends Mechanic>>();
        triggersManager = new TriggerBlockManager();
        watchBlockManager = new WatchBlockManager();
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

    /**
     * Handle a block right click event.
     * 
     * @param event
     * @return true if there was a mechanic to process the event
     */
    public boolean dispatchBlockRightClick(BlockRightClickEvent event) {
        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event))
            return false;

        // Announce the event to anyone who considers it to be on one of their
        // defining blocks
        watchBlockManager.notify(event);

        // See if this event could be occurring on any mechanism's triggering
        // blocks
        BlockWorldVector pos = toWorldVector(event.getBlock());
        try {
            Mechanic mechanic = load(pos);
            if (mechanic != null) {
                mechanic.onRightClick(event);
                return true;
            }
        } catch (InvalidMechanismException e) {
            if (e.getMessage() != null) {
                event.getPlayer().sendMessage(e.getMessage());
            }
        }

        return false;
    }

    /**
     * Handle the redstone block change event.
     * 
     * @param event
     * @return true if there was a mechanic to process the event
     */
    public boolean dispatchBlockRedstoneChange(BlockRedstoneEvent event) {
        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event))
            return false;

        // See if this event could be occurring on any mechanism's triggering
        // blocks
        BlockWorldVector pos = toWorldVector(event.getBlock());
        try {
            Mechanic mechanic = load(pos);
            if (mechanic != null) {
                mechanic.onBlockRedstoneChange(event);
                return true;
            }
        } catch (InvalidMechanismException e) {
        }
        
        return false;
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
    protected Mechanic load(BlockWorldVector pos)
            throws InvalidMechanismException {
        Mechanic mechanic = triggersManager.get(pos);

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
            triggersManager.register(pm);
            watchBlockManager.register(pm);
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
    protected Mechanic detect(BlockWorldVector pos)
            throws InvalidMechanismException {
        Mechanic mechanic = null;
        for (MechanicFactory<? extends Mechanic> factory : factories)
            if ((mechanic = factory.detect(pos)) != null)
                break;
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

    /**
     * Unload all mechanics inside the given chunk.
     * 
     * @param chunk
     */
    public void unload(BlockWorldVector2D chunk) {
        // Find mechanics that we need to unload
        Set<PersistentMechanic> applicable = triggersManager.getByChunk(chunk);
        applicable.addAll(watchBlockManager.getByChunk(chunk));
        
        for (Mechanic m : applicable) {
            unload(m);
        }
    }

    /**
     * Unload a mechanic. This will also remove the trigger points from this
     * mechanic manager.
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
            PersistentMechanic pm = (PersistentMechanic) mechanic;
            triggersManager.deregister(pm);
            watchBlockManager.deregister(pm);
        }
    }
}
