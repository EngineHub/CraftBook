package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

public class CartBooster extends CartMechanism {
    public CartBooster(double multiplier) {
        super();
        this.multiplier = multiplier;
    }
    
    private final double multiplier;
    
    public void impact(Minecart cart, Block entered, Block from) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(multiplier));
    }
}
