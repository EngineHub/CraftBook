package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class CartBooster extends CartMechanism {
    public CartBooster(double multiplier) {
        super();
        this.multiplier = multiplier;
    }
    
    private final double multiplier;
    private Vector newVelocity;
    
    public void impact(Minecart cart, Block entered, Block from) {
        if (multiplier > 1) {
            newVelocity = cart.getVelocity().normalize().multiply(multiplier);
        } else if (multiplier < 1) {
            newVelocity = cart.getVelocity().multiply(multiplier);
        } else {
            return;
        }
        cart.setVelocity(newVelocity);
    }
}
