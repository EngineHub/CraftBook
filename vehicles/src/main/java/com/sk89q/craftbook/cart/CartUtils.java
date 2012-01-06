package com.sk89q.craftbook.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public abstract class CartUtils {
    public static void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }
    
    public static void stop(Minecart cart) {
        cart.setVelocity(new Vector(0,0,0));
    }
}
