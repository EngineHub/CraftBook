package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartEjector extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block thingy = entered.getFace(BlockFace.DOWN, 1);
        Block director = pickDirector(thingy, "eject");
        if (director == null) return;

        cart.eject();
    }
}
