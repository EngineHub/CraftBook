
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

import static com.sk89q.craftbook.bukkit.BukkitUtil.toLocation;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * This mechanism allow players to toggle the fire on top of Netherrack.
 *
 * @author sk89q
 */
public class Netherrack extends Mechanic {
    
	public static class Factory implements MechanicFactory<Netherrack> {
        public Factory() {
        }
        
        @Override
        public Netherrack detect(BlockWorldVector pt) {
            int type = pt.getWorld().getBlockTypeIdAt(toLocation(pt));
            
            if (type == BlockID.NETHERRACK) {
                return new Netherrack(pt);
            }
            
            return null;
        }
    }
	
    /**
     * Construct the mechanic for a location.
     * 
     * @param pt
     */
    private Netherrack(BlockWorldVector pt) {
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
     * Unload this mechanic.
     */
    @Override
    public void unload() {
    }
	
    /**
     * Check if this mechanic is still active.
     */
    @Override
    public boolean isActive() {
        return false;
    }
	
}