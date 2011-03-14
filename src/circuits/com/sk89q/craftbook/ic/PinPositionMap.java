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

    /**
     * A PinPositionMap handles [1..getSize()] pins, inclusive.
     * 
     * @return how many pins this PinPositionMap handles.
     */
    public int getSize();
}
