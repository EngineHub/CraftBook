package com.sk89q.craftbook.cart;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.util.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.bukkit.*;

import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartStation extends CartMechanism {
    public void impact(Minecart cart, CartMechanismBlocks blocks) {
        // validate
        if (cart == null) return;
        if (!blocks.matches("station")) return;
        
        // go
        switch (isActive(blocks.rail, blocks.base, blocks.sign)) {
            case ON:
                // standardize its speed and direction.
                launch(cart, blocks.sign);
                break;
            case OFF:
            case NA:
                // park it.
                stop(cart);
                // recenter it
                Location l = blocks.rail.getLocation();
                l.setX(l.getX()+0.5);
                l.setY(l.getY()+0.5);
                l.setZ(l.getZ()+0.5);
                if (!cart.getLocation().equals(l))
                    cart.teleport(l);
                // recentering and parking almost completely prevents more than one cart from getting onto the same station.
                break;
        }
    }
    
    private void launch(Minecart cart, Block director) {
        cart.setVelocity(FUUUUUUUUUUUUUUUUU(SignUtil.getFacing(director)));
    }
    
    /**
     * WorldEdit's Vector type collides with Bukkit's Vector type here.  It's not pleasant.
     */
    public static Vector FUUUUUUUUUUUUUUUUU(BlockFace face) {
        return new Vector(face.getModX()*0.05, face.getModY()*0.05, face.getModZ()*0.05);
    }
    
    // someday: stations with an additional flag on the third line could autolaunch when players enter a parked cart.
    
}
