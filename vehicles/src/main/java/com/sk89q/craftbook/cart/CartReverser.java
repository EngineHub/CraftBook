package com.sk89q.craftbook.cart;

import org.bukkit.entity.Minecart;

import com.sk89q.craftbook.RedstoneUtil.Power;

import static com.sk89q.craftbook.cart.CartUtils.reverse;

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
            reverse(cart);
        } /*else {
            if (!blocks.getSign().getLine(1).equalsIgnoreCase("[Reverse]") || !SignUtil.isCardinal(blocks.sign)) {
                reverse(cart);
            } else {
                Block dir = blocks.sign.getRelative(BlockFace.SELF);
                
                if (dir.getLocation().getDirection() != cart.getLocation().getDirection()) reverse(cart);
            }
        }
        */
    }
}
