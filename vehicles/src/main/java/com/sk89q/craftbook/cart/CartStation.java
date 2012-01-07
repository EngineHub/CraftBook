package com.sk89q.craftbook.cart;


import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import com.sk89q.craftbook.util.SignUtil;

import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartStation extends CartMechanism {
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
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
