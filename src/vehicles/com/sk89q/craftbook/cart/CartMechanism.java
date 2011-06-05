package com.sk89q.craftbook.cart;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.*;

/**
 * Implementers of CartMechanism are intended to be singletons and do all their
 * logic at interation time (like non-persistant mechanics, but allowed zero
 * state even in RAM). In order to be effective, configuration loading in
 * MinecartManager must be modified to include an implementer.
 * 
 * @author hash
 * 
 */
public abstract class CartMechanism {
    /**
     * Represents the power input state of a cart mechanism.
     */
    public enum Power { 
        /** No power supply is connected.  (This may cause a mechanism to either default to its ON or OFF behavior or do something else entirely; it depends on the mechanism. */
        NA,
        /** At least one power supply is connected, and at least one power supply is on. */  
        ON,
        /** At least one power supply is connected, and zero power supplies are on. */
        OFF
    }
    
    public abstract void impact(Minecart cart, Block entered, Block from);
    
    /**
     * Determins if a cart mechanism should be enabled.
     * 
     * @param base the block on which the rails sit
     * @return true if no redstone is attached to the block; otherwise, true if
     *         powered and false if unpowered.
     */
    public boolean isActive(Block base) {
        boolean isWired = false;
        for (BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
            Boolean b = RedstoneUtil.isBlockFacePowered(base, face);
            if (b == Boolean.TRUE) return true;
            if (b == Boolean.FALSE) isWired = true;
            //TODO ...we need so much better code for dealing with redstone directions it's not even funny.
        }
        return (!isWired);
    }
}
