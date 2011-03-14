package com.sk89q.craftbook.ic;

import org.bukkit.block.*;
import com.sk89q.craftbook.util.*;

/**
 * Maps the indices of input and output pins in a LogicChipState to Blocks in
 * the World. There should only need be one instance of PinPositionMap per
 * family of IC; in other words, the methods for translating between pins and
 * positions in the world should be stateless and reentrant.
 * 
 * @author hash
 * 
 */
public interface PinPositionMap {
    /**
     * @param center
     *            must contain the sign that defines the IC.
     * @param pin
     * @return the block that should contain the redstone wire matching the
     *         given pin index for this IC (or null, if the pin index is out of
     *         bounds).
     */
    public Block getBlock(Block center, int pin);
    
    
    
    
    public static class FZISO implements PinPositionMap {
        public Block getBlock(Block center, int pin) {
            switch (pin) {
            case 1:     // output 1
                return center.getFace(SignUtil.getBack(center), 2);
            default:
                return null;
            }
        }
    }
    
    public static class FSISO implements PinPositionMap {
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
}
