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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;



/**
 * Handler for elevators.
 *
 * @author sk89q
 */
public class Elevator extends Mechanic {
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
    public Elevator(BlockWorldVector pt, MechanismsPlugin plugin) {
        super();
        this.pt = pt;
        this.plugin = plugin;
    }
    /**
     * Returns whether this lift has a destination.
     * 
     * @param pt
     * @param up
     */
    public static boolean hasLinkedLift(BlockWorldVector pt, boolean up) {
    	World w = pt.getWorld();
        int x = pt.getX();
        int y = pt.getY();
        int z = pt.getZ();

        if (up) {
            // Need to traverse up to find the next sign to teleport to
            for (int y1 = y + 1; y1 <= 127; y1++) {
                if (w.getBlockTypeIdAt(x, y1, z) == BlockID.WALL_SIGN
                        && getSign(w, new BlockWorldVector(w, x, y1, z), up) != null) {
                    return true;
                }
            }
        } else {
            // Need to traverse downwards to find a sign below
            for (int y1 = y - 1; y1 >= 1; y1--) {
                if (w.getBlockTypeIdAt(x, y1, z) == BlockID.WALL_SIGN
                        && getSign(w, new BlockWorldVector(pt.getWorld(), x, y1, z), up) != null) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Attempts to lift a player up or down.
     * 
     * @param player
     * @param pt
     * @param up
     */
    public static void performLift(Player player, BlockWorldVector pt, boolean up) {
        World w = pt.getWorld();
    	int x = pt.getX();
        int y = pt.getY();
        int z = pt.getZ();

        if (up) {
            // Need to traverse up to find the next sign to teleport to
            for (int y1 = y + 1; y1 <= 127; y1++) {
                if (w.getBlockTypeIdAt(x, y1, z) == BlockID.WALL_SIGN
                        && checkLift(player, w, new BlockWorldVector(w, x, y1, z), up)) {
                    return;
                }
            }
        } else {
            // Need to traverse downwards to find a sign below
            for (int y1 = y - 1; y1 >= 1; y1--) {
                if (w.getId(x, y1, z) == BlockID.WALL_SIGN
                        && checkLift(player, w, new BlockWorldVector(w, x, y1, z), up)) {
                    return;
                }
            }
        }
    }

    /**
     * Get a corresponding lift sign. Returns null if none available.
     * 
     * @param player
     * @param pt
     * @param up
     * @return
     */
    private static Sign getSign(World w, BlockWorldVector pt, boolean up) {

        int x = pt.getX();
        int y1 = pt.getY();
        int z = pt.getZ();

        Block cBlock = w.getBlockAt(x, y1, z);

        // This should not happen, but we need to check regardless
        if (!(cBlock instanceof Sign)) {
            return null;
        }
        
        Sign sign = (Sign)cBlock;

        // Found our stop?
        if (sign.getLine(1).equalsIgnoreCase("[Lift Up]")
                || sign.getLine(1).equalsIgnoreCase("[Lift Down]")
                || sign.getLine(1).equalsIgnoreCase("[Lift]")) {
            return sign;
        }
        
        return null;
    }

    /**
     * Jump to a sign above.
     * 
     * @param player
     * @param pt
     * @param up
     * @return
     */
    private static boolean checkLift(Player player, World w, BlockWorldVector pt, boolean up) {
        //int x = pt.getX();
        int y1 = pt.getY();
        //int z = pt.getZ();

        Sign sign = getSign(w, pt, up);

        // Found our stop?
        if (sign != null) {
            // We are going to be teleporting to the same place as the player
            // is currently, except with a shifted Y
            int plyX = (int)Math.floor(player.getLocation().getX());
            //int plyY = (int)Math.floor(player.getY());
            int plyZ = (int)Math.floor(player.getLocation().getZ());

            int y2;
            
            int foundFree = 0;
            boolean foundGround = false;
            
            int startingY = BlockID.canPassThrough(w.getBlockTypeIdAt(plyX, y1 + 1, plyZ))
                ? y1 + 1 : y1;

            // Step downwards until we find a spot to stand
            for (y2 = startingY; y2 >= y1 - 5; y2--) {
                int id = w.getBlockTypeIdAt(plyX, y2, plyZ);

                // We have to find a block that the player won't fall through
                if (!BlockID.canPassThrough(id)) {
                    foundGround = true;
                    break;
                }
                
                foundFree++;
            }
            
            if (foundFree < 2) {
                player.sendMessage(ChatColor.GOLD + "Uh oh! You would be obstructed!");
                return false;
            }
            
            if (!foundGround) {
                player.sendMessage(ChatColor.GOLD + "Uh oh! You would have nothing to stand on!");
                return false;
            }

            // Teleport!
            player.teleportTo(new Location(player.getWorld(), player.getLocation().getX(), y2 + 1, player.getLocation().getZ(),
                    (float)player.getLocation().getYaw(), (float)player.getLocation().getPitch()));


            // Now, we want to read the sign so we can tell the player
            // his or her floor, but as that may not be avilable, we can
            // just print a generic message
            String title = sign.getLine(0);

            if (title.length() != 0) {
                player.sendMessage(ChatColor.GOLD + "Floor: " + title);
            } else {
                player.sendMessage(ChatColor.GOLD + "You went "
                        + (up ? "up" : "down") + " a floor.");
            }

            return true;
        }

        return false;
    }

	@Override
	public void unload() {
		
	}

	@Override
	public boolean isActive() {
		return false;
	}
}
