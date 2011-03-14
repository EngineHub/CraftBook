package com.sk89q.craftbook.ic.families;

import org.bukkit.block.*;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.ic.logic.*;
import com.sk89q.craftbook.util.*;

public class FSISO implements ICFamily<LogicChipState> {
    /**
     * Package-visible: we expect to always be able to just use the singleton instance in ICFamily.
     */
    FSISO() {}
    
    public LogicChipState getState(Block center) {
        ChipState cs = new ChipState.Basic(PPM.getSize());
        for (int i = 1; i < PPM.getSize(); i++)
            cs.set(i, PPM.getBlock(center, i).isBlockPowered());        //FIXME i want a method that tells me if it's a redstone block and if its powered, damn it.
        return new LCS(cs);
    }
    
    public void applyState(LogicChipState state, Block center) {
        for (int i = LCS.inputs+1; i < PPM.getSize(); i++)
            PPM.getBlock(center, i);    // .setWirePowerLevel(cs.get(i))        //FIXME i want a method that sets the power level of a block if it's redstone, damn it.
    }
    
    public static final PPM PPM = new PPM();    ///lol
    
    
    
    
    
    public static class PPM implements PinPositionMap {
        private PPM() {}
        
        public int getSize() {
            return 2;
        }
        
        public Block getBlock(Block center, int pin) {
            switch (pin) {
            case 1:     // input 1
                return center.getFace(SignUtil.getFront(center), 1);
            case 2:     // output 1
                return center.getFace(SignUtil.getBack(center), 2);
            default:
                return null;
            }
        }
    }
    
    
    
    public static class LCS implements LogicChipState {
        private LCS(ChipState cs) {
            super();
            this.cs = cs;
        }
        
        private final ChipState cs;
        private static final int inputs = 1;
        
        public boolean getIn(int n) {
            return get(n-1);
        }
        
        public boolean getOut(int n) {
            return get(n-1+inputs);
        }
        
        public void setOut(int n, boolean value) {
            set(n-1+inputs, value);
        }
        
        /** 
         * Delegates directly to the backing ChipState. Use of LogicChipState's
         * more specific {@link #getIn(int)} and {@link #getOut(int)} methods is
         * preferred.
         */
        public boolean get(int pin) {
            return cs.get(pin);
        }

        /**
         * Delegates directly to the backing ChipState. Use of LogicChipState's
         * more specific {@link #setOut(int, boolean)} method is preferred.
         */
        public void set(int pin, boolean value) {
            cs.set(pin, value);
        }
    }
}
