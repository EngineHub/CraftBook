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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.Colors;
import com.sk89q.craftbook.access.BlockEntity;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.Vector;

/**
 * Handler for elevators.
 *
 * @author sk89q
 */
public class Elevator {

    /**
     * Returns whether this lift has a destination.
     *
     * @param pt
     * @param up
     */
    public static boolean hasLinkedLift(WorldInterface w, Vector pt, boolean up) {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        if (up) {
            // Need to traverse up to find the next sign to teleport to
            for (int y1 = y + 1; y1 <= 127; y1++) {
                if (w.getId(x, y1, z) == BlockType.WALL_SIGN
                        && getSign(w, new Vector(x, y1, z), up) != null) {
                    return true;
                }
            }
        } else {
            // Need to traverse downwards to find a sign below
            for (int y1 = y - 1; y1 >= 1; y1--) {
                if (w.getId(x, y1, z) == BlockType.WALL_SIGN
                        && getSign(w, new Vector(x, y1, z), up) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Attempt to lift a player up.
     *
     * @param player
     * @param pt
     * @param up
     */
    public static void performLift(PlayerInterface player, WorldInterface w, Vector pt, boolean up) {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        if (up) {
            // Need to traverse up to find the next sign to teleport to
            for (int y1 = y + 1; y1 <= 127; y1++) {
                if (w.getId(x, y1, z) == BlockType.WALL_SIGN
                        && checkLift(player, w, new Vector(x, y1, z), up)) {
                    return;
                }
            }
        } else {
            // Need to traverse downwards to find a sign below
            for (int y1 = y - 1; y1 >= 1; y1--) {
                if (w.getId(x, y1, z) == BlockType.WALL_SIGN
                        && checkLift(player, w, new Vector(x, y1, z), up)) {
                    return;
                }
            }
        }
    }

    /**
     * Get a corresponding lift sign or null.
     *
     * @param player
     * @param pt
     * @param up
     *
     * @return
     */
    private static SignInterface getSign(WorldInterface w, Vector pt, boolean up) {

        int x = pt.getBlockX();
        int y1 = pt.getBlockY();
        int z = pt.getBlockZ();

        BlockEntity cBlock = w.getBlockEntity(x, y1, z);

        // This should not happen, but we need to check regardless
        if (!(cBlock instanceof SignInterface)) {
            return null;
        }

        SignInterface sign = (SignInterface) cBlock;

        // Found our stop?
        if (sign.getLine2().equalsIgnoreCase("[Lift Up]")
                || sign.getLine2().equalsIgnoreCase("[Lift Down]")
                || sign.getLine2().equalsIgnoreCase("[Lift]")) {
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
     *
     * @return
     */
    private static boolean checkLift(PlayerInterface player, WorldInterface w, Vector pt, boolean up) {
        //int x = pt.getBlockX();
        int y1 = pt.getBlockY();
        //int z = pt.getBlockZ();

        SignInterface sign = getSign(w, pt, up);

        // Found our stop?
        if (sign != null) {
            // We are going to be teleporting to the same place as the player
            // is currently, except with a shifted Y
            int plyX = (int) Math.floor(player.getPosition().getX());
            //int plyY = (int)Math.floor(player.getY());
            int plyZ = (int) Math.floor(player.getPosition().getZ());

            int y2;

            int foundFree = 0;
            boolean foundGround = false;

            int startingY = BlockType.canPassThrough(w.getId(plyX, y1 + 1, plyZ))
                    ? y1 + 1 : y1;

            // Step downwards until we find a spot to stand
            for (y2 = startingY; y2 >= y1 - 5; y2--) {
                int id = w.getId(plyX, y2, plyZ);

                // We have to find a block that the player won't fall through
                if (!BlockType.canPassThrough(id)) {
                    foundGround = true;
                    break;
                }

                foundFree++;
            }

            if (foundFree < 2) {
                player.sendMessage(Colors.GOLD + "Uh oh! You would be obstructed!");
                return false;
            }

            if (!foundGround) {
                player.sendMessage(Colors.GOLD + "Uh oh! You would have nothing to stand on!");
                return false;
            }

            // Teleport!
            player.setPosition(new Vector(player.getPosition().getX(), y2 + 1, player.getPosition().getZ()),
                    (float) player.getYaw(), (float) player.getPitch());


            // Now, we want to read the sign so we can tell the player
            // his or her floor, but as that may not be avilable, we can
            // just print a generic message
            String title = sign.getLine1();

            if (title.length() != 0) {
                player.sendMessage(Colors.GOLD + "Floor: " + title);
            } else {
                player.sendMessage(Colors.GOLD + "You went "
                        + (up ? "up" : "down") + " a floor.");
            }

            return true;
        }

        return false;
    }
}
