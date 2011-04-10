package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartReverser extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block director = pickDirector(entered.getFace(BlockFace.DOWN, 1), "reverse");
        if (director == null) { //'simple' reverser
            reverse(cart);
            return;
        }
        Sign sign = (Sign) director.getState();
        // we only reverse if the cart is coming it directly the wrong facing
        if (SignUtil.getFront(director) == from.getFace(entered)) {
            reverse(cart);
        }
    }
}
