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
import org.bukkit.util.*;

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

    public static void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }

    public static void stop(Minecart cart) {
        cart.setVelocity(new Vector(0,0,0));
    }
}
