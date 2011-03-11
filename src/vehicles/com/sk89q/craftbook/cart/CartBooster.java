package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

public class CartBooster implements CartMechanism {
    public CartBooster(double multiplier) {
        this.multiplier = multiplier;
    }
    
    private final double multiplier;
    
    public void impact(Minecart cart, Block entered) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(multiplier));
    }
}
