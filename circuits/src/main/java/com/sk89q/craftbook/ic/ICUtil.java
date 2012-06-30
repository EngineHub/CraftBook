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


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
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

	    if (!state)
		    newData = data & 0x7;
	    else
		    newData = data | 0x8;

	    if (newData != data) {
		    block.setData((byte)newData, true);
		    net.minecraft.server.Block nmsBlock = net.minecraft.server.Block.byId[Material.LEVER.getId()];
		    net.minecraft.server.World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();

		    // Note: The player argument isn't actually used by the method in BlockLever, but I pass it anyway, use null if you don't have a player.
		    // This method takes care of all the necessary block updates and redstone events.
		    nmsBlock.interact(nmsWorld, block.getX(), block.getY(), block.getZ(), null);

		    return true;
	    }
	    return false;
    }
}
