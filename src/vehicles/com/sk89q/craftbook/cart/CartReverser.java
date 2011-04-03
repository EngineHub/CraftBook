// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

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
