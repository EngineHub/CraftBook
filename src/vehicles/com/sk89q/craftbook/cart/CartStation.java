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
        }
    }
    
    private void launch(Minecart cart, Block director) {
        cart.setVelocity(FUUUUUUUUUUUUUUUUU(SignUtil.getFacing(director)));
    }
    
    public static BlockVector FUUUUUUUUUUUUUUUUU(BlockFace face) {
        return new BlockVector(face.getModX(), face.getModY(), face.getModZ());
    }
    
    // ought to have an autolaunch-when-enter option someday.
    
}
