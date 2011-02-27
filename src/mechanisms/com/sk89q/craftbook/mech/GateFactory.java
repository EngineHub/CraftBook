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

package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class GateFactory implements MechanicFactory<Gate> {
    
    protected MechanismsPlugin plugin;
    
    public GateFactory(MechanismsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Gate detect(BlockWorldVector pt) {
        Block block = pt.getWorld().getBlockAt(BukkitUtil.toLocation(pt));
        if (block.getTypeId() == BlockID.WALL_SIGN) {
            BlockState state = block.getState();
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                if (sign.getLine(1).equalsIgnoreCase("[Gate]")
                        || sign.getLine(1).equalsIgnoreCase("[DGate]")) {
                    // this is a little funky because we don't actually look for the blocks
                    // that make up the movable parts of the gate until we're running the 
                    // event later... so the factory can succeed even if the signpost doesn't
                    // actually operate any gates correctly.  but it works!
                    return new Gate(pt, plugin,
                            sign.getLine(1).equalsIgnoreCase("[DGate]"));
                }
            }
        }
        
        return null;
    }

}
