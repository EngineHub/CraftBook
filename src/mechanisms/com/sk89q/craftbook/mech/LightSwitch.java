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
import org.bukkit.event.player.*;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.MechanismsConfiguration.*;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone
 * torches (off) to redstone torches (on) to normal torches. This is done every
 * time a sign with [|] or [I] is right-clicked by a player.
 *
 * @author fullwall
 * @author wizjany
 */
public class LightSwitch extends AbstractMechanic {
    public static class Factory extends AbstractMechanicFactory<LightSwitch> {

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
        
        /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException 
         */
        @Override
        public LightSwitch detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {
            String line = sign.getLine(1);
            
            if (line.equalsIgnoreCase("[|]") || line.equalsIgnoreCase("[I]")) {
                if (!player.hasPermission("craftbook.mech.light-switch")) {
                    throw new InsufficientPermissionsException();
                }
                
                sign.setLine(1, "[I]");
                player.print("Light switch created.");
            } else {
                return null;
            }
            
            throw new ProcessedMechanismException();
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
    private MechanismsConfiguration.LightSwitchSettings settings;

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
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().lightSwitchSettings.enable) return;
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(pt)) return; //wth? our manager is insane
        if (!toggleLights(pt)) {
            event.getPlayer().sendMessage("The lightswitch needs a torch above it.");
        }
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


        if (aboveID == BlockID.TORCH //|| aboveID == BlockID.REDSTONE_TORCH_OFF
                || aboveID == BlockID.REDSTONE_TORCH_ON) {
        	// Check what kind of torch the block above is.
        	// Used to get what to change torches to.
            int on = 0;
            if (aboveID == BlockID.TORCH) {
                on = 2;
            } else if (aboveID == BlockID.REDSTONE_TORCH_ON) {
                on = 1;
            //} else if (aboveID == BlockID.REDSTONE_TORCH_OFF) {
            //    on = 0;
            } else { //not a lightswitch
                return true;
            }
            // Prevent spam
            Long lastUse = recentLightToggles.remove(pt);
            long currTime = System.currentTimeMillis();
            if (lastUse != null && currTime - lastUse < 500) {
                recentLightToggles.put(pt, lastUse);
                return true;
            }
            recentLightToggles.put(pt, currTime);
            int changed = 0;
            int offset = plugin.getLocalConfiguration().lightSwitchSettings.radius;
            for (int x =  wx - offset; x <= offset + wx; x++) {
                for (int y = wy - offset; y <= offset + wy; y++) {
                    for (int z = wz - offset; z <= offset + wz; z++) {
                        int id = world.getBlockTypeIdAt(x, y, z);
                        if (id == BlockID.TORCH //|| id == BlockID.REDSTONE_TORCH_OFF
                                || id == BlockID.REDSTONE_TORCH_ON) {
                            // Limit the maximum number of changed lights
                            if (changed >= plugin.getLocalConfiguration().lightSwitchSettings.changed) {
                                return true;
                            }
                            if (on == 2) { //bright -> dim
                            	world.getBlockAt(x, y, z).setTypeId(BlockID.REDSTONE_TORCH_ON);
                            } else if (on == 1) { //dim -> bright
                            	world.getBlockAt(x, y, z).setTypeId(BlockID.TORCH);
                            } //else if (on == 0) { //off -> dim
                                //world.getBlockAt(x, y, z).setTypeId(BlockID.REDSTONE_TORCH_ON);
                            //}
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
