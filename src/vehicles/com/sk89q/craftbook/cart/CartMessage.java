package com.sk89q.craftbook.cart;

import org.bukkit.entity.*;
import org.bukkit.block.*;

import static com.sk89q.craftbook.cart.CartUtils.pickDirector;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;
import com.sk89q.craftbook.util.*;

/**
 * Mechanism that sends a message to the player in a minecart
 * which passes over a [Print] sign.
 *
 * @author wizjany
 */

public class CartMessage extends CartMechanism {
    public CartMessage(VehiclesPlugin plugin) {
        this.plugin = plugin;
    }

    private VehiclesPlugin plugin;

    public void impact(Minecart cart, Block entered, Block from) {
        if (plugin.getLocalConfiguration().minecartMessageEmitters && cart.getPassenger() instanceof Player) {
            Block director = pickDirector(entered.getFace(BlockFace.DOWN, 1), "print");
            if (director == null) return;
            Sign sign = (Sign) director.getState();

            String line = sign.getLine(0) + sign.getLine(2) + sign.getLine(3);
            ((Player) cart.getPassenger()).sendMessage(line);
        }
    }
}
