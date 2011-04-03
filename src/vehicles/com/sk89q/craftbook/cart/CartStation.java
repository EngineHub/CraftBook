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
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartStation extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block thingy = entered.getFace(BlockFace.DOWN, 1);
        Block director = pickDirector(thingy, "station");
        if (director == null) return;

        if (isActive(thingy)) {
            // standardize its speed and direction.
            launch(cart, director);
        } else {
            // park it.
            stop(cart);
            //cart.teleport(entered.getLocation());     // i'd really love to enforce centering on this, but in practice rounding errors and such seem to be rapeful.
        }
    }

    private void launch(Minecart cart, Block director) {
        cart.setVelocity(FUUUUUUUUUUUUUUUUU(SignUtil.getFacing(director)));
    }

    public static Vector FUUUUUUUUUUUUUUUUU(BlockFace face) {
        return new Vector(face.getModX()*0.1, face.getModY()*0.1, face.getModZ()*0.1);
    }

    // ought to have an autolaunch-when-enter option today.
    public void enterLaunch(Minecart cart) {
        Block director = pickDirector(cart.getLocation().getBlock().getFace(BlockFace.DOWN), "station");
        launch(cart, director);
    }
}
