package com.sk89q.craftbook.mech;

import java.util.*;

import org.bukkit.event.block.*;
import org.bukkit.util.BlockVector;

/**
 * <p>
 * A Mechanic is a an object that manages a set of BlockVectors to enhance those
 * positions with craftbook functionality.
 * </p>
 * 
 * <p>
 * A Mechanic at minimum has one or more BlockVector which it wishes to receive
 * events from -- these are called "triggers". These BlockVector are also
 * registered with a MechanicManager when the Mechanic is loaded into that
 * MechanicManager.
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
 * otherwise interferred with in the intervening time -- this is so that
 * Mechanic instances can be discarded when their containing chunks are
 * unloaded, as well as for general insurance against server crashes.
 * </p>
 * 
 * @author hash
 * 
 */

// OUTSTANDING ISSUES:
//	- What about integrity during saves if a Mechanic stretching across chunk boundaries is saved in different states in each chunk due to save concurrency control failure?
//	- Pretty much everything, actually.

public abstract class Mechanic {
    /**
     * @param $triggers the positions of blocks which can trigger the Mechanic to take action.
     */
    // this constructor makes dang sure Mechanic implementers aren't allowed to change their mind about where they pay attention
    //  anything else would require a Mechanic to know its MechanicManager so it could keep their concept of triggers in sync, and that's just messily cyclic and unnecessary.
    protected Mechanic(BlockVector... $triggers) {
	this.$triggers = Collections.unmodifiableList(Arrays.asList($triggers));
    }

    /**
     * These BlockVector also get loaded into a HashMap in a MechanicManager
     * when the Mechanic is loaded. The Mechanic still has to keep the list of
     * the triggerable BlockVectors in order to deregister from the HashMap
     * efficiently when unloaded. (Historically, these triggering BlockVector
     * have typically contained signs, but there's no serious technical
     * limitation that it must be so -- once a Mechanic is loaded,
     * MechanicManager will pass it even events that the MechanicManager itself
     * wouldn't care about.)
     */
    private final List<BlockVector> $triggers;
    
    public List<BlockVector> getTriggerPositions() {
	return $triggers;
    }
    
    // i toyed with the idea of having Mechanics expose a map from BlockVector $trigger to BlockListener
    //  but in most cases a Mechanic won't have more than one BlockListener even if it has multiple triggers
    //  and if it does, it can do the demuxing internally by just getting the dang vector out of the event anyway.
    // but i am fairly confident that it's going to best for a Mechanic to expose a whole BlockListener instead of this stupid method i've thrown in here as a placeholder.
    public void dealWithIt(BlockEvent $evt) {}
    
    
    
    // i think it's going to be the reponsibility of a Mechanic to commit suicide when it detects a block that defines it to be destroyed
    // or in some cases just reconfigure itself (moving the exact destination block of an elevator for example).
    //  which... has a lot of implications.
    //	first of all, the set of "defining" blocks can be larger than the set of triggering blocks, and i think multiple Mechanic instances can have overlapping defining blocks as well.
    //      so that would require MechanicManager to report (at least some kinds of) events to multiple Mechanics if the event's aren't absorbed by a trigger block.
    //      and note that the set of "defining" blocks can still be a subset of the total set of blocks that the Mechanic enhances!
    //      should we have different BlockListeners for events on defining blocks and triggering blocks?
    //      and oh dear god if Mechanics overlap in ther defining blocks... i could see danger of either infinite recursion or else changes without appropriate notifiction.
    //  second of all, that requires that the Mechanic have some way to scream at its Manager that it wants to die, which would seem to imply that the cyclic pointers i said i'd rather avoid earlier might be necessary after all.
}
