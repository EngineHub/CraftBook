package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

/**
 * Implementers of CartMechanism are intended to be singletons and do all their
 * logic at interation time (like non-persistant mechanics, but allowed zero
 * state even in RAM). In order to be effective, the inner class of this
 * interface that enumerates them must be modified to include an implementer.
 * 
 * @author hash
 * 
 */
public interface CartMechanism {
    public void impact(Minecart cart, Block entered);
}
