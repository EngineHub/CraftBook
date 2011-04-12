package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.Location;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartStation extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block thingy = entered.getFace(BlockFace.DOWN, 1);
        Block director = pickDirector(thingy, "station");
        if (director == null) return;

        Byte data = entered.getData();
        Location loc = entered.getLocation();
        Boolean adjustAxis = true;
        if (data == 1 || data == 2 || data == 3) {
           adjustAxis = false;
        }
        if (adjustAxis) {
           loc.setX(loc.getX() + 0.5D);
           loc.setZ(loc.getZ() + 0.5D);
        }
        cart.teleport(loc);
        stop(cart);
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
