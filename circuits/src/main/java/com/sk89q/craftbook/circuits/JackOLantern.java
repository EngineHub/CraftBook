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

import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.*;

/**
 * This mechanism allow players to toggle Jack-o-Lanterns.
 *
 * @author sk89q
 */
public class JackOLantern extends AbstractMechanic {
	
	private int originalId;
	
    public static class Factory extends AbstractMechanicFactory<JackOLantern> {
        public Factory() {
        }
        
        @Override
        public JackOLantern detect(BlockWorldVector pt) {
            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));
            
            if (type == BlockID.PUMPKIN || type == BlockID.JACKOLANTERN) {
                return new JackOLantern(pt);
            }
            
            return null;
        }
    }
    
    /**
     * Construct the mechanic for a location.
     * 
     * @param pt
     */
    private JackOLantern(BlockWorldVector pt) {
        super();
        originalId = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));
    }
    
    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        byte data;
        
        data = event.getBlock().getData();
        if (event.getNewCurrent() > 0) {
            event.getBlock().setTypeId(BlockID.JACKOLANTERN);
        } else {
            event.getBlock().setTypeId(BlockID.PUMPKIN);
        }
        event.getBlock().setData(data, false);
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

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		event.getBlock().setTypeId(originalId);
		event.getBlock().breakNaturally();
		event.setCancelled(true);
	}
}
