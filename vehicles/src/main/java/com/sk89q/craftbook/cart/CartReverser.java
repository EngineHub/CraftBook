package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

import com.sk89q.craftbook.RedstoneUtil.*;
import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartReverser extends CartMechanism {
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        
        // care?
        if (minor) return;
        
        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

        // go
        if (blocks.sign == null) {
            // there's no restrictions on when we reverse
            reverse(cart);
        } else {
            if (!blocks.matches("[Reverse]")) {
                // i dunno what it is, but it doesn't restrict reverse
                reverse(cart);
            } else {
                // we only reverse if the cart is coming in directly the wrong facing (i.e. a diagonal sign has zero effect).
                switch (SignUtil.getFront(blocks.sign)) {
                case NORTH: case SOUTH:
                    cart.getVelocity().multiply(new Vector(-1,-1,0));
                    break;
                case EAST: case WEST:
                    cart.getVelocity().multiply(new Vector(0,-1,-1));
                    break;
                default: // narp.
                }
            }
        }
    }
}
