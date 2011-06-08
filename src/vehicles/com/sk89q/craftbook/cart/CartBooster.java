package com.sk89q.craftbook.cart;

import static com.sk89q.craftbook.cart.CartUtils.stop;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.RedstoneUtil.*;

public class CartBooster extends CartMechanism {
    public CartBooster(double multiplier) {
        super();
        this.multiplier = multiplier;
    }
    
    private final double multiplier;
    
    public void impact(Minecart cart, CartMechanismBlocks blocks) {
        // validate
        if (cart == null) return;
        
        // go
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;
        
        cart.setVelocity(cart.getVelocity().normalize().multiply(multiplier));
    }
}
