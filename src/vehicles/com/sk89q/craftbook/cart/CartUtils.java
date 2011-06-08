package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

import com.sk89q.craftbook.util.*;

public abstract class CartUtils {
    public static void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }
    
    public static void stop(Minecart cart) {
        cart.setVelocity(new Vector(0,0,0));
    }
}
