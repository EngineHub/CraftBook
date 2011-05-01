package com.sk89q.craftbook.cart;

import org.bukkit.entity.*;
import org.bukkit.block.*;

import static com.sk89q.craftbook.cart.CartUtils.pickDirector;
import static com.sk89q.craftbook.util.SignUtil.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;
import com.sk89q.craftbook.util.*;

/**
 * Mechanism that sends a message to the player in a minecart
 * which passes over a [Print] sign. Cardinal-facing signs are
 * uni-directional, all other signs will always trigger.
 */
public class CartMessageEmitter extends CartMechanism {
    public CartMessageEmitter(VehiclesPlugin plugin) {
        this.plugin = plugin;
    }

    private VehiclesPlugin plugin;

    public void impact(Minecart cart, Block entered, Block from) {
        if (plugin.getLocalConfiguration().minecartMessageEmitters && cart.getPassenger() instanceof Player) {
            Block director = pickDirector(entered.getFace(BlockFace.DOWN, 1), "print", 0);
            if (director == null) return;

            if (isCardinal(director)) {
                if (!(director.getFace(getFront(director)).getFace(BlockFace.UP, 2).getLocation().equals(from.getLocation()))
                    && !(director.getFace(getFront(director)).getFace(BlockFace.UP, 3).getLocation().equals(from.getLocation()))) {
                    return;
                }
            }
            Sign sign = (Sign) director.getState();
            String line = sign.getLine(1) + sign.getLine(2) + sign.getLine(3);
            ((Player) cart.getPassenger()).sendMessage(line);
        }
    }
}
