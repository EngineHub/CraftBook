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
import org.bukkit.event.player.*;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.*;

/**
 * The default elevator mechanism -- wall signs in a vertical column that
 * teleport the player vertically when triggered.
 * 
 * @author sk89q
 * @author hash
 * 
 */
public class Elevator extends AbstractMechanic {
    public static class Factory extends AbstractMechanicFactory<Elevator> {
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }
        
        private MechanismsPlugin plugin;

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
        @Override
        public Elevator detect(BlockWorldVector pt) throws InvalidMechanismException {
            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            Direction dir = isLift(block);
            switch (dir) {
            case UP:
            case DOWN:
                return new Elevator(block, dir, plugin);
            case RECV:
                throw new NoDepartureException();
            case NONE:
            default:    // there are no uncovered cases, i don't know why eclipse insists this be here
                return null;
            }
        }
        
        /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException 
         */
        @Override
        public Elevator detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {   
            Direction dir = isLift(sign.getBlock());
            switch (dir) {
                case UP:
                    if (!player.hasPermission("craftbook.mech.elevator")) {
                        throw new InsufficientPermissionsException();
                    }
                    
                    player.print("Elevator up sign created.");
                    sign.setLine(1, "[Lift Up]");
                    break;
                case DOWN:
                    if (!player.hasPermission("craftbook.mech.elevator")) {
                        throw new InsufficientPermissionsException();
                    }
                    
                    player.print("Elevator down sign created.");
                    sign.setLine(1, "[Lift Down]");
                    break;
                case RECV:
                    if (!player.hasPermission("craftbook.mech.elevator")) {
                        throw new InsufficientPermissionsException();
                    }
                    
                    player.print("Elevator target sign created.");
                    sign.setLine(1, "[Lift]");
                    break;
                default:    // there are no uncovered cases, i don't know why eclipse insists this be here
                    return null;
            }
            
            throw new ProcessedMechanismException();
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
    private Elevator(Block trigger, Direction dir, MechanismsPlugin plugin) throws InvalidMechanismException {
        super();
        this.trigger = trigger;
        this.plugin = plugin;
        
        // find destination sign
        shift = (dir == Direction.UP) ? BlockFace.UP : BlockFace.DOWN;
        int f = (dir == Direction.UP) ? 127 : 0;
        destination = trigger;
        if (destination.getY() == f)             // heading up from top or down from bottom
            throw new InvalidConstructionException();
        while (true) {
            destination = destination.getFace(shift);
            Direction derp = isLift(destination);
            if (derp != Direction.NONE) break;   // found it!
            if (destination.getY() == 127)       // hit the top of the world
                throw new InvalidConstructionException();
            if (destination.getY() == 0)         // hit the bottom of the world
                throw new InvalidConstructionException();
        }
        // and if we made it here without exceptions, destination is set.
        
        // finding solid ground is deferred until a click event comes in
        // since we teleport the player straight up, and the sign can be
        // clicked from blocks other than the ones directly in the elevator 
        // shaft.
    }
    
    private MechanismsPlugin plugin;
    
    private Block trigger;
    private BlockFace shift;
    private Block destination;
    public static enum Direction {
        NONE, UP, DOWN, RECV;
    }
    
    
    
    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().elevatorSettings.enable) return;
        
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger))) return; //wth? our manager is insane
        
        LocalPlayer localPlayer = plugin.wrap(event.getPlayer());
        
        if (!localPlayer.hasPermission("craftbook.mech.elevator.use")) {
            localPlayer.printError("You don't have permission to use elevators.");
            return;
        }
        
        makeItSo(event.getPlayer());
        
        event.setCancelled(true);
    }
    
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
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
        // well, unless that's already a ceiling.
        if (!occupiable(floor)) floor = floor.getFace(BlockFace.DOWN);
        
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
            if (floor.getY() == 0x0)        // hit the bottom of the world
                break;
            floor = floor.getFace(BlockFace.DOWN);
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
        player.teleport(subspaceRift);
        
        // Now, we want to read the sign so we can tell the player
        // his or her floor, but as that may not be avilable, we can
        // just print a generic message
        String title = ((Sign)destination.getState()).getLines()[0];
        if (title.length() != 0) {
            player.sendMessage("Floor: " + title);
        } else {
            player.sendMessage("You went " + (shift.getModY() > 0 ? "up" : "down") + " a floor.");
        }
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
    
    
    
    @Override
    public void unload() {
        /* we're not persistent */
    }
    
    @Override
    public boolean isActive() {
        /* we're not persistent */
        return false;
    }

    private static class NoDepartureException extends InvalidMechanismException {
        private static final long serialVersionUID = 3845311158458450314L;

        public NoDepartureException() {
            super("Cannot depart from this lift (can only arrive).");
        }
    }

    private static class InvalidConstructionException extends
            InvalidMechanismException {
        private static final long serialVersionUID = 2306504048848430689L;

        public InvalidConstructionException() {
            super("This lift has no destination.");
        }
    }
}
