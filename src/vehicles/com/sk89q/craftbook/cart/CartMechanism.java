package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

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
    public abstract void impact(Minecart cart, Block entered, Block from);

    /**
     * Determins if a cart mechanism should be enabled.
     * 
     * @param base the block on which the rails sit
     * @return true if no redstone is attached to the block; otherwise, true if
     *         powered and false if unpowered.
     */
    public boolean isActive(Block base) {
        
        //TODO
        return true;
    }
}
