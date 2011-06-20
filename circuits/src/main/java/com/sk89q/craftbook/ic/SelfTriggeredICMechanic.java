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

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.*;

public class SelfTriggeredICMechanic extends ICMechanic implements SelfTriggeringMechanic {
    
    private SelfTriggeredIC selfTrigIC;
    
    public SelfTriggeredICMechanic(CircuitsPlugin plugin, String id, SelfTriggeredIC ic,
            ICFamily family, BlockWorldVector pos) {
        super(plugin, id, ic, family, pos);
        this.selfTrigIC = ic;
    }

    @Override
    public void think() {
        BlockWorldVector pt = getTriggerPositions().get(0);
        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
        BlockState state = block.getState();
        
        if (state instanceof Sign) {
            // Assuming that the plugin host isn't going wonky here
            ChipState chipState = family.detect(pt, (Sign) state);
            selfTrigIC.think(chipState);
        }
    }

}
