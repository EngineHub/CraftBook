// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.circuits;

import java.util.Random;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * This mechanism allow players to read bookshelves and get a random line
 * from a file as as "book."
 *
 * @author sk89q
 */
public class Nettherrack extends Mechanic {

    /**
     * Used for picking random lines.
     */
    protected static Random rand = new Random();
    
    /**
     * Construct a bookcase for a location.
     * 
     * @param pt
     */
    public Nettherrack(BlockWorldVector pt) {
        super();
    }
    
    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block above = event.getBlock().getRelative(0, 1, 0);
        
        if (event.getNewCurrent() > 0) {
            if (above.getTypeId() == 0) {
                above.setTypeId(BlockID.FIRE);
            }
        } else {
            if (above.getTypeId() == BlockID.FIRE) {
                above.setTypeId(BlockID.AIR);
            }
        }
    }
    
    /**
     * Unload this bookcase.
     */
    @Override
    public void unload() {
    }

    /**
     * Check if this bookcase is still active.
     */
    @Override
    public boolean isActive() {
        return false;
    }

}
