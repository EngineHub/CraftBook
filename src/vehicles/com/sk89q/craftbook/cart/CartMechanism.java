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

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.*;

/**
 * Implementers of CartMechanism are intended to be singletons and do all their
 * logic at interation time (like non-persistant mechanics, but allowed zero
 * state even in RAM). In order to be effective, configuration loading in
 * MinecartManager must be modified to include an implementer.
 *
 * @author hash
 *
 */
public abstract class CartMechanism {
    public abstract void impact(Minecart cart, Block entered, Block from);

    /**
     * Determins if a cart mechanism should be enabled.
     *
     * @param base the block on which the rails sit
     * @return true if no redstone is attached to the block; otherwise, true if
     *         powered and false if unpowered.
     */
    public boolean isActive(Block base) {
        boolean isWired = false;
        for (BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
            Boolean b = RedstoneUtil.isBlockFacePowered(base, face);
            if (b == Boolean.TRUE) return true;
            if (b == Boolean.FALSE) isWired = true;
            //TODO ...we need so much better code for dealing with redstone directions it's not even funny.
        }
        return (!isWired);
    }
}
