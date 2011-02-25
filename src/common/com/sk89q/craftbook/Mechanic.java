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
import org.bukkit.event.block.BlockRightClickEvent;
import com.sk89q.craftbook.util.BlockWorldVector;

/**
 * A Mechanic is a an object that manages a set of BlockVectors to enhance those
 * positions with CraftBook functionality.
 * 
 * <p>
 * A Mechanic at minimum has one or more BlockVector which it wishes to receive
 * events from -- these are called "triggers". These BlockVector are also
 * registered with a MechanicManager when the Mechanic is loaded into that
 * MechanicManager.
 * </p>
 * 
 * <p>
 * A Mechanic may also have a set of BlockVector which it wishes to receive
 * events from because they may change the state of the mechanism, but they
 * aren't explicitly triggers -- these are called "defining". An example of
 * defining blocks are the vertical column of blocks in an elevator mechanism,
 * since the operation of the elevator depends on where there is open space in
 * blocks that are clearly not trigger blocks. Defining blocks are also
 * registered with a MechanicManager, but are distinct from trigger blocks in
 * that a single BlockVector may be considered defining to multiple mechanisms.
 * </p>
 * 
 * <p>
 * A Mechanic may alter or examine the contents of BlockVector other than those
 * that are explicitly trigger or definer BlockVector. These are the BlockVector
 * that the Mechanic "enhances", but there is no formal interface in which these
 * must be enumerated.
 * </p>
 * 
 * <p>
 * Mechanic instances are subject to lazy instantiation and must be able to
 * derive all of their internal state from the blocks in the world at any time;
 * however, they exist purely to keep such internal state since the
 * instantiation may be relatively expensive. Mechanic instances should be able
 * to be discarded at essentially any time and without warning, and yet be able
 * to provide correct service whenever a new Mechanic instance is created over
 * the same BlockVector unless the contents of the BlockVector have been
 * otherwise interfered with in the intervening time -- this is so that
 * Mechanic instances can be discarded when their containing chunks are
 * unloaded, as well as for general insurance against server crashes.
 * </p>
 * 
 * @author sk89q
 * @author hash
 */
public abstract class Mechanic {

    /**
     * These BlockVector also get loaded into a HashMap in a MechanicManager
     * when the Mechanic is loaded. The Mechanic still has to keep the list of
     * the triggerable BlockVectors in order to deregister from the HashMap
     * efficiently when unloaded.
     */
    protected final List<BlockWorldVector> triggers;

    /**
     * Construct the object.
     * 
     * @param triggers positions that can trigger this mechanic
     */
    protected Mechanic(BlockWorldVector ... triggers) {
        this.triggers = Collections.unmodifiableList(Arrays.asList(triggers));
    }

    /**
     * Get the list of trigger positions that this mechanic uses. This list
     * cannot change during the run of the mechanic ever.
     * 
     * @return
     */
    public final List<BlockWorldVector> getTriggerPositions() {
        return triggers;
    }
    
    /**
     * Unload this mechanic. This should free any allocated resources and
     * de-initialize. This may be called once the mechanic no longer exists
     * in the world.
     */
    public abstract void unload();
    
    /**
     * Return true if this mechanic is still active in the world. For example,
     * for a gate, it should check to see if the [Gate] sign still exists
     * at the trigger points.
     * 
     * @return
     */
    public abstract boolean isActive();
    
    /**
     * Think. This is called every 2 ticks if this mechanic is self-triggering.
     * Implementation should be aware that the mechanic may no longer exist
     * in the world.
     */
    public void think() {
    }
    
    /**
     * Raised when a block is right clicked.
     * @param event
     */
    public void onRightClick(BlockRightClickEvent event) {
    }
}
