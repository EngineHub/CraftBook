package com.sk89q.craftbook.ic.families;

import org.bukkit.block.*;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.ic.families.FSISO.*;
import com.sk89q.craftbook.ic.families.FZISO.*;
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
    public static final FZISO FZISO = new FZISO();
    public static final FSISO FSISO = new FSISO();
    
    
    
    
    
    
    abstract static class LogicICFamily implements ICFamily<LogicChipState> {
        LogicICFamily(PinPositionMap PPM) {
            this.PPM = PPM;
        }
        
        public final PinPositionMap PPM;
        
        // the LogicChipState getState(Block) method has to remain completely in the subclasses
        // because they have the specific instance of LogicChipState internally
        // the applyState(LogicChipState, Block) method can be abstracted out to here because it uses the polymorphism of the LCS made in getState(Block).
        
        public void applyState(LogicChipState state, Block center) {
            for (int i = state.inputSize()+1; i < PPM.getSize(); i++)
                PPM.getBlock(center, i);    // .setWirePowerLevel(cs.get(i))        //FIXME i want a method that sets the power level of a block if it's redstone, damn it.
        }
    }
}
