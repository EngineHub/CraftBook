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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockRedstoneEvent;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Mechanic wrapper for ICs. The mechanic manager dispatches events to this
 * mechanic, and then it is processed and passed onto the associated IC.
 * 
 * @author sk89q
 */
public class ICMechanic extends PersistentMechanic {
    
    protected final MechanismsPlugin plugin;
    protected final Block center;
    protected final IC ic;
    
    /**
     * 
     * @param plugin
     * @param ic
     * @param center I swear to god if this isn't a sign, fire and brimstone will rain
     */
    public ICMechanic(MechanismsPlugin plugin, IC ic, Block center) {
        this.plugin = plugin;
        this.ic = ic;
        this.center = center;
    }
    
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        // Mind you!  an ICMechanic can get events from more than just the sign that is its defining center, if it's a bigshit IC.
        // Therefore, never assume that the event's block is the center of an IC, or even a sign block for that matter.
        
        Runnable runnable = new Runnable() {
            public void run() {
                if (isActive()) return;        // we've become invalid and TODO ought to commit suicide
                                               //   ... kind of a costly check to be running so often :/
                
                ChipState chipState = ic.getFamily().getState(center);
                ic.trigger(chipState);
                ic.getFamily().applyState(chipState, center);
            }
        };
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, 2);
    }
    
    @Override
    public void unload() {
        ic.unload();
    }
    
    @Override
    public boolean isActive() {
        if (center.getTypeId() == BlockID.WALL_SIGN) {
            Sign sign = (Sign) center.getState();
            
            Matcher matcher = ICMechanicFactory.codePattern.matcher(sign.getLine(1));
            
            if (matcher.matches()) {
                //FIXME son of a bitch why can't i get the IC's ID here?  it's in ICFactory.  Should it be?
                //return matcher.group(1).equalsIgnoreCase(ic.g);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {
        return new ArrayList<BlockWorldVector>();
    }

}
