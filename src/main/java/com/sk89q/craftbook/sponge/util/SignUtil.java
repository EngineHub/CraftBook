// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.sponge.util;

import jdk.nashorn.internal.ir.Block;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.util.Direction;

/**
 * <p>
 * Convenience methods for dealing with some sign block data.
 * </p>
 * <p>
 * If you intend to care about the eight further directions (as opposed to the four cardinal directions and the four ordinal directions), this isn't
 * for you -- since Direction has no such directions, those will be rounded to the nearest ordinal direction. (If the term "further direction"
 * confuses you, see https://secure.wikimedia.org/wikipedia/en/wiki/Cardinal_directions).
 * </p>
 * <p>
 * This is direly close to being a replicate of things you can access via org.bukkit.material.Sign (which extends MaterialData). However, that thing:
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
public class SignUtil {

    public static boolean isSign(BlockLoc block) {

        return block.getType() == BlockTypes.STANDING_SIGN || block.getType() == BlockTypes.WALL_SIGN;
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the direction a player would be facing when reading the sign; i.e. the face that is actually the back
     * side of the sign.
     */
    public static Direction getFacing(BlockLoc sign) {

        return getBack(sign);
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the side of the sign containing the text (in other words, when a player places a new sign,
     * while facing north, this will return south).
     */
    public static Direction getFront(BlockLoc sign) {

        if (sign.getType() == BlockTypes.STANDING_SIGN) {
            switch (sign.getState().getDataValue()) {
                case 0x0:
                    return Direction.SOUTH;
                case 0x1:
                case 0x2:
                case 0x3:
                    return Direction.SOUTHWEST;
                case 0x4:
                    return Direction.WEST;
                case 0x5:
                case 0x6:
                case 0x7:
                    return Direction.NORTHWEST;
                case 0x8:
                    return Direction.NORTH;
                case 0x9:
                case 0xA:
                case 0xB:
                    return Direction.NORTHEAST;
                case 0xC:
                    return Direction.EAST;
                case 0xD:
                case 0xE:
                case 0xF:
                    return Direction.SOUTHEAST;
                default:
                    return null;
            }
        } else {
            switch (sign.getState().getDataValue()) {
                case 0x2:
                    return Direction.NORTH;
                case 0x3:
                    return Direction.SOUTH;
                case 0x4:
                    return Direction.WEST;
                case 0x5:
                    return Direction.EAST;
                default:
                    return null;
            }
        }
    }

    public static BlockLoc getFrontBlock(BlockLoc sign) {

        return sign.getRelative(getFront(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the blank side of the sign opposite the text. In the case of a wall sign,
     * the block in this direction is the block to which the sign is
     * attached. This is also the direction a player would be facing when reading the sign; see {@link #getFacing(Block)}.
     */
    public static Direction getBack(BlockLoc sign) {

        if (sign.getType() == BlockTypes.STANDING_SIGN) {
            switch (sign.getState().getDataValue()) {
                case 0x0:
                    return Direction.NORTH;
                case 0x1:
                case 0x2:
                case 0x3:
                    return Direction.NORTHEAST;
                case 0x4:
                    return Direction.EAST;
                case 0x5:
                case 0x6:
                case 0x7:
                    return Direction.SOUTHEAST;
                case 0x8:
                    return Direction.SOUTH;
                case 0x9:
                case 0xA:
                case 0xB:
                    return Direction.SOUTHWEST;
                case 0xC:
                    return Direction.WEST;
                case 0xD:
                case 0xE:
                case 0xF:
                    return Direction.NORTHWEST;
                default:
                    return null;
            }
        } else {
            switch (sign.getState().getDataValue()) {
                case 0x2:
                    return Direction.SOUTH;
                case 0x3:
                    return Direction.NORTH;
                case 0x4:
                    return Direction.EAST;
                case 0x5:
                    return Direction.WEST;
                default:
                    return null;
            }
        }
    }

    public static BlockLoc getBackBlock(BlockLoc sign) {

        return sign.getRelative(getBack(sign));
    }

    public static BlockLoc getNextSign(BlockLoc sign, String criterea, int searchRadius) {

        BlockLoc otherBlock = sign;
        Direction way = getBack(sign);
        boolean found = false;
        for (int i = 0; i < searchRadius; i++) {
            if (isSign(otherBlock.getRelative(way))) {
                otherBlock = otherBlock.getRelative(way);
                // TODO if (BukkitUtil.toChangedSign(otherBlock).getLine(1).equalsIgnoreCase(criterea)) {
                found = true;
                break;
                // }
            } else otherBlock = otherBlock.getRelative(way);
        }
        if (!found) return null;
        return otherBlock;
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the cardinal or ordinal direction to a player's left as they face the sign to read it; if the sign is
     * oriented in a further direction,
     * the result is rounded to the nearest ordinal direction.
     */
    public static Direction getRight(BlockLoc sign) {

        if (sign.getType() == BlockTypes.STANDING_SIGN) {
            switch (sign.getState().getDataValue()) {
                case 0x0:
                    return Direction.EAST;
                case 0x1:
                case 0x2:
                case 0x3:
                    return Direction.SOUTHEAST;
                case 0x4:
                    return Direction.SOUTH;
                case 0x5:
                case 0x6:
                case 0x7:
                    return Direction.SOUTHWEST;
                case 0x8:
                    return Direction.WEST;
                case 0x9:
                case 0xA:
                case 0xB:
                    return Direction.NORTHWEST;
                case 0xC:
                    return Direction.NORTH;
                case 0xD:
                case 0xE:
                case 0xF:
                    return Direction.NORTHEAST;
                default:
                    return null;
            }
        } else {
            switch (sign.getState().getDataValue()) {
                case 0x2:
                    return Direction.WEST;
                case 0x3:
                    return Direction.EAST;
                case 0x4:
                    return Direction.SOUTH;
                case 0x5:
                    return Direction.NORTH;
                default:
                    return null;
            }
        }
    }

    public static BlockLoc getLeftBlock(BlockLoc sign) {

        return sign.getRelative(getLeft(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the cardinal or ordinal direction to a player's right they face the sign to read it; if the sign is
     * oriented in a further direction, the
     * result is rounded to the nearest ordinal direction.
     */
    public static Direction getLeft(BlockLoc sign) {

        if (sign.getType() == BlockTypes.STANDING_SIGN) {
            switch (sign.getState().getDataValue()) {
                case 0x0:
                    return Direction.WEST;
                case 0x1:
                case 0x2:
                case 0x3:
                    return Direction.NORTHWEST;
                case 0x4:
                    return Direction.NORTH;
                case 0x5:
                case 0x6:
                case 0x7:
                    return Direction.NORTHEAST;
                case 0x8:
                    return Direction.EAST;
                case 0x9:
                case 0xA:
                case 0xB:
                    return Direction.SOUTHEAST;
                case 0xC:
                    return Direction.SOUTH;
                case 0xD:
                case 0xE:
                case 0xF:
                    return Direction.SOUTHWEST;
                default:
                    return null;
            }
        } else {
            switch (sign.getState().getDataValue()) {
                case 0x2:
                    return Direction.EAST;
                case 0x3:
                    return Direction.WEST;
                case 0x4:
                    return Direction.NORTH;
                case 0x5:
                    return Direction.SOUTH;
                default:
                    return null;
            }
        }
    }

    public static BlockLoc getRightBlock(BlockLoc sign) {

        return sign.getRelative(getRight(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return true if the sign is oriented along a cardinal direction (or if it's a wall sign,
     * since those are always oriented along cardinal
     * directions); false otherwise.
     */
    public static boolean isCardinal(BlockLoc sign) {

        if (sign.getType() == BlockTypes.STANDING_SIGN) {
            switch (sign.getState().getDataValue()) {
                case 0x0:
                case 0x4:
                case 0x8:
                case 0xC:
                    return true;
                default:
                    return false;
            }
        } else return true;
    }

    /**
     * @param face Start from direction
     * @return clockwise direction
     */
    public static Direction getClockWise(Direction face) {

        switch (face) {
            case NORTH:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case WEST:
                return Direction.NORTH;

            default:
                return null;
        }
    }

    /**
     * @param face Start from direction
     * @return clockwise direction
     */
    public static Direction getCounterClockWise(Direction face) {

        switch (face) {
            case NORTH:
                return Direction.WEST;
            case EAST:
                return Direction.NORTH;
            case SOUTH:
                return Direction.EAST;
            case WEST:
                return Direction.SOUTH;

            default:
                return null;
        }
    }

    /* From this point on - replacements for ChangedSign in CB 3.x */

    public static String getTextRaw(Sign sign, int line) {

        Text text = getText(sign, line);
        if(text instanceof Literal)
            return ((Literal) text).getContent();
        return text.toString();
    }

    public static Text getText(Sign sign, int line) {

        return sign.getLine(line);
    }
}
