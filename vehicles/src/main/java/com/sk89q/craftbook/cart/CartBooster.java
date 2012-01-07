package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.RedstoneUtil.Power;

import org.bukkit.util.Vector;
import org.bukkit.entity.Minecart;

public class CartBooster extends CartMechanism {
    public CartBooster(double multiplier) {
        super();
        this.multiplier = multiplier;
    }
    
    private final double multiplier;
    
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        
        // care?
        if (minor) return;
        
        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;
        

        // speed up or down
        Vector newVelocity;
        if (multiplier > 1) {
            newVelocity = cart.getVelocity().normalize().multiply(multiplier);
        } else if (multiplier < 1) {
            newVelocity = cart.getVelocity().multiply(multiplier);
        } else {
            return;
        }
        // go
        cart.setVelocity(newVelocity);
    }
}
