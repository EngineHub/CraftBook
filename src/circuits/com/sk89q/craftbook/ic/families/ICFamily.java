package com.sk89q.craftbook.ic.families;

import org.bukkit.block.*;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.ic.logic.*;

public interface ICFamily<CST extends ChipState> {
    /**
     * Examines the world around the given block to produce a ChipState
     * representing the power state of all the pins.
     * 
     * @param center
     *            the block that contains the sign defining an IC.
     * @return a ChipState (or a subtype with expanded functionality useful to
     *         the family)
     */
    public CST getState(Block center);

    /**
     * 
     * @param state
     *            a ChipState to apply (implementers are free to only apply some
     *            pins; LogicChipState for example will only ever change the
     *            state of its output pins).
     * @param center
     *            the block that contains the sign defining an IC.
     */
    public void applyState(CST state, Block center);
    
    
    
    
    
    // these could be specified in a map and picked out by strings or something
    //  but there's really no reason for that level of indirection because ICs always know which family they're a member of at compile time anyway
    //  and as far as extensibility by third parties, well, they can keep their ICFamily singletons wherever they want.  craftbook doesn't need to know about it an any central place.
    public static final ICFamily<LogicChipState> FZISO = new FZISO();
    public static final ICFamily<LogicChipState> FSISO = new FSISO();
}
