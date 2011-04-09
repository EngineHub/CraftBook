package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.Location;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;
import static com.sk89q.craftbook.util.SignUtil.*;

public class CartEjector extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block thingy = entered.getFace(BlockFace.DOWN, 1);
        Block director = pickDirector(thingy, "eject");
        if (director == null) return;

        if (cart.getPassenger() == null) return;
        if (((Sign) director.getState()).getLine(2).equalsIgnoreCase("here")) {
            cart.eject();
            return;
        } else {
            Entity passenger = cart.getPassenger();
            Location ejectToBlock = director.getFace(getBack(director)).getFace(BlockFace.UP, 2).getLocation();
            Location ejectTo = centerBlock(new Location(passenger.getWorld(), ejectToBlock.getX(), ejectToBlock.getY(),
                         ejectToBlock.getZ(), passenger.getLocation().getYaw(), passenger.getLocation().getPitch()));
            cart.eject();
            passenger.teleport(ejectTo);
        }
    }
}
