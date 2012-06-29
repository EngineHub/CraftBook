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


import com.sk89q.worldedit.BlockWorldVector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Attachable;

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
    public static boolean setState(BlockWorldVector source, Block block, boolean state) {
        if (block.getType() != Material.LEVER) return false;
		return updateLever(source, block, state);
    }

	private static boolean updateLever(BlockWorldVector source, Block outputBlock, boolean state) {
		if (updateBlockData(outputBlock, state)) {
			outputBlock.getState().update();
			Block b = Bukkit.getWorld(source.getWorld().getName()).getBlockAt(source.getBlockX(), source.getBlockY(), source.getBlockZ());
			byte oldData = b.getData();
			byte notData;
			if (oldData>1) notData = (byte)(oldData-1);
			else if (oldData<15) notData = (byte)(oldData+1);
			else notData = 0;
			b.setData(notData, true);
			b.setData(oldData, true);
			return true;
		}
		return false;
	}

	private static boolean updateBlockData(Block b, boolean state) {
		byte data = b.getData();
		boolean oldLevel = ((data&0x08) > 0);
		if (oldLevel==state) return false;

		byte newData = (byte)(state? data | 0x8 : data & 0x7);

		b.setData(newData, true);

		return true;
	}
}
