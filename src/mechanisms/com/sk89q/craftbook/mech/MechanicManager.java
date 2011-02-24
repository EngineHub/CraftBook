package com.sk89q.craftbook.mech;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.util.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;

/**
 * <p>
 * A MechanicManager tracks the BlockVector where loaded Mechanic instances have
 * registered triggerability, and disbatches incoming events by checking for
 * Mechanic instance that might be triggered by the event and by considering
 * instantiation of a new Mechanic instance for unregistered BlockVector.
 * </p>
 * 
 * @author hash
 * 
 */

//OUTSTANDING ISSUES:
//	- Should we let Mechanics register their concern for specific different types of event?
//		Probably not.  If a Mechanic claims a BlockVector as triggerable, nobody else is ever going to get the event even if that Mechanic turns out not to care about it.
//	- Type safety with BlockEvent subtypes:
//		Losing type information by casting everything back to a BlockEvent is an extreme case of DO NOT WANT.
//		However, do we really want to force every Mechanic to also have a BlockListener for every BlockVector that's a trigger?  Seems a little inconvenient.
//	- Not sure if the register/register functions for Mechanic are going to be private... right now it seems like the Manager might also be the factory, or at least directly on the path to it, so they probably can/should be, but that may change.
//	- World.  BlockVector ignores it.  Which would be fine if we could instantiate a new MechanicManager for each world and register listeners on a per-world basis, but I don't think we can do that.
//		Bukkit's Location class seems... um, an odd choice to use in specifying Block locations, since they can't have pitch and yaw and their x/y/z aren't doubles either.

public class MechanicManager extends BlockListener {
    public MechanicManager() {
	$triggers = new HashMap<BlockVector, Mechanic>();
    }

    private static final boolean CHECK_SANITY = true;
    private final Map<BlockVector, Mechanic> $triggers;
    
    
    
    @Override
    public void onBlockRightClick(BlockRightClickEvent event) {
	if (disbatchToMechanic(event)) return;
	
	// consider making a new Mechanic.
	// i imagine this will tend to have slightly different conditions for different event types for efficiency.
	switch (event.getBlock().getType()) {
		case SIGN:
		    // read the sign and do string ops to see what kind of Mechanic might be appropriate to instantiate.
		    // if we should make: do so, then register, then pass the event, and we're done.
		    // the details of detection should be left up the Mechanic implementations themselves as much as possible, which might mean we need kind of an ugly loop through attempting a bunch of factory methods here.
		    break;
		case BOOKSHELF:
		    // bookshelves are so simple and stateless they might actually be most efficiently done with purely static methods, even though that would represent a slight break in pattern.  that and bookshelf blocks tend to be fairly spammed.
		    break;
		default: // don't care.
	}
	
	// i think it might be best if Mechanic gets a reference to BaseBukkitPlugin somehow and does the player-wrapping sort of things on its own, if it needs them.
	//  many Mechanic implementers won't need to futz with those extra object allocations... and more importantly it means we don't have to make a whole dang set of listening interfaces for passing the wrapped object along with other objects that contain the unwrapped originals, because that's just too redundant for comfort.
    }
    
    
    
    private void registerMechanic(Mechanic $m) {
	if (CHECK_SANITY) 
            for (BlockVector $p : $m.getTriggerPositions())
        	if ($triggers.get($p) != null) throw new IllegalStateException("Position "+$p+" has already been claimed by another Mechanic; registration not performed.");
	for (BlockVector $p : $m.getTriggerPositions())
	    $triggers.put($p, $m);
    }
    
    // MechanicManager is allowed to unload and discard a Mechanic without telling the Mechanic about it.  this means removing all of that Mechanic's triggering BlockVectors from the triggerDispatch map.
    private void deregisterMechanic(Mechanic $m) {
	if (CHECK_SANITY) 
            for (BlockVector $p : $m.getTriggerPositions())
        	if ($triggers.get($p) != $m) throw new IllegalStateException("Position "+$p+" has occupied by another Mechanic; deregistration not performed.");
	for (BlockVector $p : $m.getTriggerPositions())    
	    $triggers.put($p, null);
    }
    
    /**
     * @return true if a Mechanic was found to eat the event (and processing the
     *         event should thus stop); false otherwise.
     */
    private boolean disbatchToMechanic(BlockEvent event) {	// see the note at the top of file about type.
	Mechanic $m = $triggers.get(BukkitUtil.toVector(event.getBlock()));
	if ($m == null) return false;
	$m.dealWithIt(event);
	return true;
    }
}
