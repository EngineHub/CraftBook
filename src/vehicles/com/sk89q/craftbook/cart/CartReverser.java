package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartReverser extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block director = pickDirector(entered.getFace(BlockFace.DOWN, 1), "reverse");
        if (director == null) return;
        Sign sign = (Sign) director.getState();
        
        if (sign == null)
            // there's no restrictions on when we reverse
            reverse(cart);
        else {
            // we only reverse if the cart is coming it directly the wrong facing (i.e. a diagonal sign has zero effect).
            if (SignUtil.getFront(director) == from.getFace(entered))
                reverse(cart);
        }
    }
}
