package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartStation extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block thingy = entered.getFace(BlockFace.DOWN, 1);
        Block director = pickDirector(thingy, "station");
        if (director == null) return;
        
        if (isActive(thingy)) {
            // standardize its speed and direction.
            launch(cart, director);
        } else {
            // park it.
            stop(cart);
            //cart.teleport(entered.getLocation());     // i'd really love to enforce centering on this, but in practice rounding errors and such seem to be rapeful.
        }
    }
    
    private void launch(Minecart cart, Block director) {
        cart.setVelocity(FUUUUUUUUUUUUUUUUU(SignUtil.getFacing(director)));
    }
    
    public static Vector FUUUUUUUUUUUUUUUUU(BlockFace face) {
        return new Vector(face.getModX()*0.1, face.getModY()*0.1, face.getModZ()*0.1);
    }
    
    // ought to have an autolaunch-when-enter option someday.
    
}
