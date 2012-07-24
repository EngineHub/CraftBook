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

package com.sk89q.craftbook.util;

import com.sk89q.craftbook.access.BlockEntity;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;

public class MinecraftUtil {

    /**
     * Gets the block behind a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     *
     * @return
     */
    public static Vector getWallSignBack(WorldInterface w, Vector pt, int multiplier) {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = w.getData(x, y, z);
        if (data == 0x2) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x3) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else {
            return new Vector(x - multiplier, y, z);
        }
    }

    /**
     * Gets the block behind a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     *
     * @return
     */
    public static Vector getSignPostOrthogonalBack(WorldInterface w, Vector pt, int multiplier) {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = w.getData(x, y, z);
        if (data == 0x8) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x0) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else if (data == 0xC) { // South
            return new Vector(x - multiplier, y, z);
        } else {
            return null;
        }
    }

    /**
     * Gets the block next to a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     *
     * @return
     */
    public static Vector getWallSignSide(WorldInterface w, Vector pt, int multiplier) {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = w.getData(x, y, z);
        if (data == 0x2) { // East
            return new Vector(x + multiplier, y, z);
        } else if (data == 0x3) { // West
            return new Vector(x - multiplier, y, z);
        } else if (data == 0x4) { // North
            return new Vector(x, y, z - multiplier);
        } else {
            return new Vector(x, y, z + multiplier);
        }
    }

    /**
     * Checks whether a sign at a location has a certain text on a
     * particular line, case in-sensitive.
     *
     * @param pt
     * @param lineNo
     * @param text
     *
     * @return
     */
    public static boolean doesSignSay(WorldInterface w, Vector pt, int lineNo, String text) {

        BlockEntity e = w.getBlockEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (e instanceof SignInterface) {
            return ((SignInterface) e).getLine(lineNo).equalsIgnoreCase(text);
        }

        return false;
    }

    public static void dropSign(WorldInterface world, int x, int y, int z) {

        world.setId(x, y, z, 0);
        world.dropItem(x, y, z, 323, 1);
    }

    public static void dropSign(WorldInterface world, Vector pt) {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        world.setId(x, y, z, 0);
        world.dropItem(x, y, z, 323, 1);
    }
}
