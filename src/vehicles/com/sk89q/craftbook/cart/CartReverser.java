package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.util.*;

public class CartReverser extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block director = null;
        Sign sign = null;
        pickDirector: {
            for (int i = 2; i <= 3; i++) {
                director = entered.getFace(BlockFace.DOWN, i);
                if (SignUtil.isSign(director)) {
                    sign = (Sign) director.getState();
                    if (sign.getLine(1).equalsIgnoreCase("[reverse]"))
                        break pickDirector; // found it
                    else
                        sign = null;
                }
            }
        }
        
        System.err.println("asdf");
        
        if (sign == null)
            // there's no restrictions on when we reverse
            reverse(cart);
        else {
            // we only reverse if the cart is coming it directly the wrong facing (i.e. a diagonal sign has zero effect).
            if (SignUtil.getFront(director) == from.getFace(entered))
                reverse(cart);
        }
    }
    
    private void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }
}
