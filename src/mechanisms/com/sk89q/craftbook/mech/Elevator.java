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

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.blocks.*;

/**
 * The default elevator mechanism -- wall signs in a vertical column that
 * teleport the player vertically when triggered.
 * 
 * @author hash
 * 
 */
public class Elevator extends Mechanic {
    public static class Factory implements MechanicFactory<Elevator> {
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }
        
        protected MechanismsPlugin plugin;

        /**
         * Explore around the trigger to find a functional elevator; throw if
         * things look funny.
         * 
         * @param pt
         *            the trigger (should be a signpost)
         * @return an Elevator if we could make a valid one, or null if this
         *         looked nothing like an elevator.
         * @throws InvalidMechanismException
         *             if the area looked like it was intended to be an
         *             elevator, but it failed.
         */
        public Elevator detect(BlockWorldVector pt) throws InvalidMechanismException {
            Block block = pt.toBlock();
            // check if this looks at all like something we're interested in first
            Direction dir = isLift(block);
            switch (dir) {
            case UP:
            case DOWN:
                return new Elevator(block, dir);
            case RECV:
                throw new NoDepartureException();
            case NONE:
            default:    // there are no uncovered cases, i don't know why eclipse insists this be here
                return null;
            }
        }
    }

    /**
     * @param trigger
     *            if you didn't already check if this is a wall sign with
     *            appropriate text, you're going on Santa's naughty list.
     * @param dir
     *            the direction (UP or DOWN) in which we're looking for a destination
     * @throws InvalidMechanismException
     */
    private Elevator(Block trigger, Direction dir) throws InvalidMechanismException {
        super();
        this.trigger = trigger;
        
        // find destination sign
        shift = (dir == Direction.UP) ? BlockFace.UP : BlockFace.DOWN;
        // this isn't documented, but deep down inside bukkit code
        // the getFace method turns into a vector shift sort of operation
        // and then the coordinate gets bounding by a bitwise and before
        // a block is actually retrieved.  So, end result: when you ask 
        // for the block above the top block in the world, you'll get the
        // same block back again!
        destination = trigger.getFace(shift);
        while (true) {
            Direction derp = isLift(destination); // interestingly, this also means if you put a [lift up] at the top of the world, it just teleports to itself.
            if (derp != Direction.NONE) break;    // found it!
            Block next = destination.getFace(shift);
            if (next == destination)              // hit the edge of the world 
                throw new InvalidConstructionException();
            destination = next;
        }
        // and if we made it here without exceptions, destination is set.
        
        // finding solid ground is deferred until a click event comes in
        // since we teleport the player straight up, and the sign can be
        // clicked from blocks other than the ones directly in the elevator 
        // shaft.
    }

    private Block trigger;
    private BlockFace shift;
    private Block destination;
    public static enum Direction {
        NONE, UP, DOWN, RECV;
    }
    
    
    
    public void onRightClick(BlockRightClickEvent event) {
        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return; //wth? our manager is insane
        makeItSo(event.getPlayer());
    }
    
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        /* we only affect players, so we don't care about redstone events */
    }
    
    private void makeItSo(Player player) {
        // start with the block shifted vertically from the player 
        // to the destination sign's height (plus one).
        Block floor = destination.getWorld().getBlockAt(
                (int)Math.floor(player.getLocation().getX()),
                destination.getY() + 1,
                (int)Math.floor(player.getLocation().getZ())
        );
        
        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (occupiable(floor)) {
                foundFree++;
            } else {
                foundGround = true;
                break;
            }
            floor = floor.getFace(shift);
            // if there's a hole through bedrock on the bottom of the map
            // then it's possible for it to be considered a valid destination
            // but i think i'll leave that as an easter egg for anyone insane
            // enough to test for that.
        }
        if (!foundGround) {
            player.sendMessage("There is no floor at the destination!");
            return;
        }
        if (foundFree < 2) {
            player.sendMessage("Your destination is obstructed.");
            return;
        }
        
        // Teleport!
        Location subspaceRift = player.getLocation().clone();
        subspaceRift.setY(floor.getY() + 1);
        player.teleportTo(subspaceRift);
        
        // Now, we want to read the sign so we can tell the player
        // his or her floor, but as that may not be avilable, we can
        // just print a generic message
        String title = ((Sign)destination.getState()).getLines()[1];
        if (title.length() != 0) {
            player.sendMessage("Floor: " + title);
        } else {
            player.sendMessage("You went " + (shift.getModY() > 0 ? "up" : "down") + " a floor.");
        }
    }
    
    
    
    private static Elevator.Direction isLift(BlockWorldVector pt) {
        return isLift(pt.toBlock());
    }
    private static Elevator.Direction isLift(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Sign)) return Direction.NONE;
        
        Sign sign = (Sign)state;
        // if you were really feeling frisky this could definitely 
        // be optomized by converting the string to a char[] and then
        // doing work
        if (sign.getLines()[1].equalsIgnoreCase("[Lift Up]")) return Direction.UP;
        if (sign.getLines()[1].equalsIgnoreCase("[Lift Down]")) return Direction.DOWN;
        if (sign.getLines()[1].equalsIgnoreCase("[Lift]")) return Direction.RECV;
        return Direction.NONE;
    }
    
    //XXX this is terrible, terrible, TERRIBLE abstraction and really should be located elsewhere.
    private static boolean occupiable(Block block) {
        final int id = block.getTypeId();
        return id == 0 // Air
                || id == 8 // Water
                || id == 9 // Water
                || id == 6 // Saplings
                || id == 37 // Yellow flower
                || id == 38 // Red flower
                || id == 39 // Brown mushroom
                || id == 40 // Red mush room
                || id == 50 // Torch
                || id == 51 // Fire
                || id == 55 // Redstone wire
                || id == 59 // Crops
                || id == 63 // Sign post
                || id == 65 // Ladder
                || id == 66 // Minecart tracks
                || id == 68 // Wall sign
                || id == 69 // Lever
                || id == 70 // Stone pressure plate
                || id == 72 // Wooden pressure plate
                || id == 75 // Redstone torch (off)
                || id == 76 // Redstone torch (on)
                || id == 77 // Stone button
                || id == 78 // Snow
                || id == 83 // Reed
                || id == 90; // Portal
    }
    
    
    
    public void unload() {
        /* we're not persistent */
    }
    
    public boolean isActive() {
        /* we're not persistent */
        return false;
    }
    
    
    
    private static class NoDepartureException extends InvalidMechanismException {
        public NoDepartureException() { super("Cannot depart from this lift (can only arrive)."); }
    }
    private static class InvalidConstructionException extends InvalidMechanismException {
        public InvalidConstructionException() { super("This lift has no destination."); }
    }
}
