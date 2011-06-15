package com.sk89q.craftbook.cart;

import org.bukkit.entity.*;
import com.sk89q.craftbook.RedstoneUtil.*;

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
        
        // go
        cart.setVelocity(cart.getVelocity().normalize().multiply(multiplier));
    }
}
