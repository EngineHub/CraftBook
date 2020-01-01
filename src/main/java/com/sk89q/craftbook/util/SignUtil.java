// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.util;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.SignChangeEvent;

/**
 * <p>
 * Convenience methods for dealing with some sign block data.
 * </p>
 * <p>
 * If you intend to care about the eight further directions (as opposed to the four cardinal directions and the four
 * ordinal directions), this isn't
 * for you -- since BlockFace has no such directions, those will be rounded to the nearest ordinal direction. (If the
 * term "further direction"
 * confuses you, see https://secure.wikimedia.org/wikipedia/en/wiki/Cardinal_directions).
 * </p>
 * <p>
 * This is direly close to being a replicate of things you can access via org.bukkit.material.Sign (which extends
 * MaterialData). However, that thing:
 * <ul>
 * <li>doesn't provide the relative direction methods.
 * <li>rounds the further divisions to cardinal/ordinal differently (and wrong, in my book).
 * <li>has the same class name for that MaterialData thing as the BlockState, which is annoying as hell import-wise.
 * <li>requires allocating an object and copying two bytes in a fashion that I consider kinda unnecessary.
 * </ul>
 * Ideally, I think I'd like to see if I can get something like these methods pushed to bukkit.
 * </p>
 *
 * @author hash
 */
public final class SignUtil {

    public static boolean isSign(Block block) {
        return isStandingSign(block) || isWallSign(block);
    }

    public static boolean isStandingSign(Block block) {
        return Tag.STANDING_SIGNS.isTagged(block.getType());
    }

    public static boolean isWallSign(Block block) {
        return Tag.WALL_SIGNS.isTagged(block.getType());
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *             if you ask about a stone block, it's considered a wall
     *             sign).
     *
     * @return the direction a player would be facing when reading the sign; i.e. the face that is actually the back
     *         side of the sign.
     */
    public static BlockFace getFacing(Block sign) {

        return getBack(sign);
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *             if you ask about a stone block, it's considered a wall
     *             sign).
     *
     * @return the side of the sign containing the text (in other words, when a player places a new sign,
     *         while facing north, this will return south).
     */
    public static BlockFace getFront(Block sign) {
        BlockData blockData = sign.getBlockData();
        if (blockData instanceof Sign) {
            return ((Sign) blockData).getRotation();
        } else if (blockData instanceof WallSign) {
            return ((WallSign) blockData).getFacing();
        } else {
            return BlockFace.SELF;
        }
    }

    public static Block getFrontBlock(Block sign) {

        return sign.getRelative(getFront(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *             if you ask about a stone block, it's considered a wall
     *             sign).
     *
     * @return the blank side of the sign opposite the text. In the case of a wall sign,
     *         the block in this direction is the block to which the sign is
     *         attached. This is also the direction a player would be facing when reading the sign; see {@link
     *         #getFacing(Block)}.
     */
    public static BlockFace getBack(Block sign) {
        return getFront(sign).getOppositeFace();
    }

    public static Block getBackBlock(Block sign) {

        return sign.getRelative(getBack(sign));
    }

    public static Block getNextSign(Block sign, String criterea, int searchRadius) {

        Block otherBlock = sign;
        BlockFace way = sign.getFace(getBackBlock(sign));
        boolean found = false;
        for (int i = 0; i < searchRadius; i++) {
            if (isSign(otherBlock.getRelative(way))) {
                otherBlock = otherBlock.getRelative(way);
                if (CraftBookBukkitUtil.toChangedSign(otherBlock).getLine(1).equalsIgnoreCase(criterea)) {
                    found = true;
                    break;
                }
            } else
                otherBlock = otherBlock.getRelative(way);
        }
        if (!found) return null;
        return otherBlock;
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *             if you ask about a stone block, it's considered a wall
     *             sign).
     *
     * @return the cardinal or ordinal direction to a player's left as they face the sign to read it; if the sign is
     *         oriented in a further direction,
     *         the result is rounded to the nearest ordinal direction.
     */
    public static BlockFace getRight(Block sign) {
        return getCounterClockWise(getFront(sign));
    }

    public static Block getLeftBlock(Block sign) {

        return sign.getRelative(getLeft(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *             if you ask about a stone block, it's considered a wall
     *             sign).
     *
     * @return the cardinal or ordinal direction to a player's right they face the sign to read it; if the sign is
     *         oriented in a further direction, the
     *         result is rounded to the nearest ordinal direction.
     */
    public static BlockFace getLeft(Block sign) {
        return getClockWise(getFront(sign));
    }

    public static Block getRightBlock(Block sign) {

        return sign.getRelative(getRight(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *             if you ask about a stone block, it's considered a wall
     *             sign).
     *
     * @return true if the sign is oriented along a cardinal direction (or if it's a wall sign,
     *         since those are always oriented along cardinal
     *         directions); false otherwise.
     */
    public static boolean isCardinal(Block sign) {
        BlockFace facing = getFront(sign);
        switch (facing) {
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                return true;
            default:
                return false;
        }
    }

    /**
     * @param face Start from direction
     *
     * @return clockwise direction
     */
    public static BlockFace getClockWise(BlockFace face) {

        switch (face) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;

            default:
                return BlockFace.SELF;
        }
    }

    /**
     * @param face Start from direction
     *
     * @return clockwise direction
     */
    public static BlockFace getCounterClockWise(BlockFace face) {

        switch (face) {
            case NORTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.SOUTH;

            default:
                return BlockFace.SELF;
        }
    }

    /**
     * Cancels a sign change event, and destroys the sign in the process.
     * 
     * @param event The event that is to be cancelled.
     */
    public static void cancelSign(SignChangeEvent event) {
        event.setCancelled(true);
        event.getBlock().breakNaturally();
    }

    /**
     * Check whether or not the block is a sign, and if so, does it contain the said text on that specific line.
     * 
     * @param sign The sign to check.
     * @param text The text to check.
     * @param line The line to check the text on.
     * @return
     */
    public static boolean doesSignHaveText(Block sign, String text, int line) {
        return isSign(sign) && CraftBookBukkitUtil.toChangedSign(sign).getLine(line).equals(text);
    }
}