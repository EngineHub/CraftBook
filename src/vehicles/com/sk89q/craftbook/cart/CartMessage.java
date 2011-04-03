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
