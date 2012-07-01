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

package com.sk89q.craftbook.ic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Lever;

/**
 * IC utility functions.
 * 
 * @author sk89q
 */
public class ICUtil {

    private ICUtil() {
    }

    /**
     * Set an IC's output state at a block.
     * 
     * @param block
     * @param state
     * @return whether something was changed
     */
    public static boolean setState(Block block, boolean state) {
        if (block.getType() != Material.LEVER) return false;
        byte data = block.getData();
        int newData;

        Block sourceBlock = block.getRelative(((Lever) block.getState().getData()).getAttachedFace());

        if (!state)
            newData = data & 0x7;
        else
            newData = data | 0x8;

        if (newData != data) {
            block.setData((byte)newData, true);
            int oldS = state ? (0) : (1);
            int newS = state ? (1) : (0);
            BlockRedstoneEvent event = new BlockRedstoneEvent(sourceBlock, oldS, newS);
            Bukkit.getPluginManager().callEvent(event);
            return true;
        }
        return false;
    }
}
