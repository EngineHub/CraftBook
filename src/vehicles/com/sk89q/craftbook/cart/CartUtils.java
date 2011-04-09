package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.Location;

import com.sk89q.craftbook.util.*;

public abstract class CartUtils {
    /**
     * Search for a "director" sign one or two blocks below the block that
     * supports the cart tracks.
     * 
     * @param base
     *            the block beneath the tracks
     * @param keyword
     *            the case-insensitive keyword to search for between brackets on
     *            the second line of the sign.
     * @return a director sign if one can be found; null otherwise.
     */
    public static Block pickDirector(Block base, String keyword) {
        for (int i = 1; i <= 2; i++) {
            Block director = base.getFace(BlockFace.DOWN, i);
            if (SignUtil.isSign(director))
                if (((Sign)director.getState()).getLine(1).equalsIgnoreCase("["+keyword+"]"))
                    return director;
        }
        return null;
    }
    
    public static Location centerBlock(Location loc) {
        Location toLoc = loc;
        toLoc.setX(loc.getX() + 0.5D);
        toLoc.setZ(loc.getZ() + 0.5D);
        return toLoc;
    }
    
    public static void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }
    
    public static void stop(Minecart cart) {
        cart.setVelocity(new Vector(0,0,0));
    }
}
