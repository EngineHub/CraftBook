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
package com.sk89q.craftbook.mech;

import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.event.block.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone
 * to normal torches. This is done every time a sign with [|] or [I] is right 
 * clicked by a player.
 *
 * @author fullwall
 */
public class LightSwitch extends Mechanic {
    public static class Factory implements MechanicFactory<LightSwitch> {

        protected MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public LightSwitch detect(BlockWorldVector pt) {
            Block block = pt.toBlock();
            // check if this looks at all like something we're interested in first
            if (block.getTypeId() != BlockID.WALL_SIGN)
                return null;
            String line = ((Sign) block.getState()).getLine(1);
            if (!line.equalsIgnoreCase("[|]") && !line.equalsIgnoreCase("[I]"))
                return null;

            // okay, now we can start doing exploration of surrounding blocks
            // and if something goes wrong in here then we throw fits.
            return new LightSwitch(pt, plugin);
        }
    }
    
    /**
     * Store a list of recent light toggles to prevent spamming. Someone
     * clever can just use two signs though.
     */
    private HistoryHashMap<BlockWorldVector,Long> recentLightToggles = new HistoryHashMap<BlockWorldVector,Long>(20);

    /**
     * Configuration.
     */
    protected MechanismsPlugin plugin;
    
    private BlockWorldVector pt;
    
    /**
     * Construct a LightSwitch for a location.
     * 
     * @param pt
     * @param plugin 
     */
    private LightSwitch(BlockWorldVector pt, MechanismsPlugin plugin) {
        super();
        this.pt = pt;
        this.plugin = plugin;
    }
    
    

    @Override
    public void onRightClick(BlockRightClickEvent event) {
        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(pt)) return; //wth? our manager is insane
        toggleLights(pt);
    }
    
    /**
     * Toggle lights in the immediate area.
     * 
     * @param pt
     * @return
     */
    private boolean toggleLights(BlockWorldVector pt) {
    	World world = pt.getWorld();
    	
    	int wx = pt.getBlockX();
        int wy = pt.getBlockY();
        int wz = pt.getBlockZ();
        int aboveID = world.getBlockTypeIdAt(wx, wy + 1, wz);
        
        if (aboveID == BlockID.TORCH || aboveID == BlockID.REDSTONE_TORCH_OFF
                || aboveID == BlockID.REDSTONE_TORCH_ON) {
        	// Check if block above is a redstone torch.
        	// Used to get what to change torches to.
            boolean on = (aboveID != BlockID.TORCH);
            // Prevent spam
            Long lastUse = recentLightToggles.remove(pt);
            long currTime = System.currentTimeMillis();
            if (lastUse != null && currTime - lastUse < 500) {
                recentLightToggles.put(pt, lastUse);
                return true;
            }
            recentLightToggles.put(pt, currTime);
            int changed = 0;
            for (int x = -10 + wx; x <= 10 + wx; x++) {
                for (int y = -10 + wy; y <= 10 + wy; y++) {
                    for (int z = -5 + wz; z <= 5 + wz; z++) {
                        int id = world.getBlockTypeIdAt(x, y, z);
                        if (id == BlockID.TORCH || id == BlockID.REDSTONE_TORCH_OFF
                                || id == BlockID.REDSTONE_TORCH_ON) {
                            // Limit the maximum number of changed lights
                            if (changed >= 20) {
                                return true;
                            }
                            if (on) {
                            	world.getBlockAt(x, y, z).setTypeId(BlockID.TORCH);
                            } else {
                            	world.getBlockAt(x, y, z).setTypeId(BlockID.REDSTONE_TORCH_ON);
                            }
                            changed++;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void unload() {
        /* No persistence. */
    }
    
    @Override
    public boolean isActive() {
        return false; /* Keeps no state */
    }
}
