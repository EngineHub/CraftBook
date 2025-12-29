/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.jspecify.annotations.Nullable;

/**
 * <p>
 * Convenience methods for dealing with some sign block data.
 * </p>
 * <p>
 * If you intend to care about the eight further directions (as opposed to the four cardinal
 * directions and the four
 * ordinal directions), this isn't
 * for you -- since BlockFace has no such directions, those will be rounded to the nearest ordinal
 * direction. (If the
 * term "further direction"
 * confuses you, see <a href="https://secure.wikimedia.org/wikipedia/en/wiki/Cardinal_directions">this page</a>).
 * </p>
 *
 * @author hash
 */
public final class SignUtil {

    private SignUtil() {
    }

    public static boolean isSign(Block block) {
        return Tag.ALL_SIGNS.isTagged(block.getType());
    }

    public static boolean isStandingSign(Block block) {
        return Tag.STANDING_SIGNS.isTagged(block.getType());
    }

    public static boolean isWallSign(Block block) {
        return isWallSign(block.getType());
    }

    public static boolean isWallSign(Material type) {
        return Tag.WALL_SIGNS.isTagged(type);
    }

    /**
     * Get the back of the sign, the block that it is attached to.
     *
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *     if you ask about a stone block, it's considered a wall
     *     sign).
     * @return the direction a player would be facing when reading the sign; i.e. the face that is
     *     actually the back
     *     side of the sign.
     * @deprecated confusing, should use getBack or getFront explicitly
     */
    @Deprecated
    public static BlockFace getFacing(Block sign) {
        return getBack(sign);
    }

    /**
     * Get the front face of the sign.
     *
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *     if you ask about a stone block, it's considered a wall
     *     sign).
     * @return the side of the sign containing the text (in other words, when a player places a new
     *     sign,
     *     while facing north, this will return south).
     */
    public static BlockFace getFront(Block sign) {
        BlockData blockData = sign.getBlockData();
        if (blockData instanceof Sign signBlock) {
            return signBlock.getRotation();
        } else if (blockData instanceof WallSign wallSign) {
            return wallSign.getFacing();
        } else {
            return BlockFace.SELF;
        }
    }

    public static Block getFrontBlock(Block sign) {
        return sign.getRelative(getFront(sign));
    }

    /**
     * Get the back face of the sign.
     *
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *     if you ask about a stone block, it's considered a wall
     *     sign).
     * @return the blank side of the sign opposite the text. In the case of a wall sign,
     *     the block in this direction is the block to which the sign is
     *     attached. This is also the direction a player would be facing when reading the sign; see
     *     {@link
     *     #getFacing(Block)}.
     */
    public static BlockFace getBack(Block sign) {
        return getFront(sign).getOppositeFace();
    }

    public static Block getBackBlock(Block sign) {
        return sign.getRelative(getBack(sign));
    }

    /**
     * This method will iterate backwards from the "back" of the sign, until it finds a block that is
     * not a sign.
     *
     * <p>
     * This is useful for allowing mechanics to have multiple signs attached, effectively allowing nested signs.
     * </p>
     *
     * @param sign The sign block to iterate from.
     * @return The first block in the chain that is not a sign.
     */
    public static Block findNonSignBackBlock(Block sign) {
        Block workingBlock = sign;

        while (SignUtil.isSign(workingBlock)) {
            // Iterate backwards from signs.
            Block backBlock = SignUtil.getBackBlock(workingBlock);

            if (backBlock.equals(workingBlock)) {
                // If we've not moved, this block is giving us invalid data.
                break;
            }

            workingBlock = backBlock;
        }

        return workingBlock;
    }

    public static @Nullable Block getNextSign(Block sign, String criterea, int searchRadius) {
        Block otherBlock = sign;
        BlockFace way = sign.getFace(getBackBlock(sign));
        boolean found = false;
        for (int i = 0; i < searchRadius; i++) {
            if (found) {
                break;
            }
            if (isSign(otherBlock.getRelative(way))) {
                otherBlock = otherBlock.getRelative(way);
                org.bukkit.block.Sign otherSign = (org.bukkit.block.Sign) otherBlock.getState(false);
                for (Side side : Side.values()) {
                    if (otherSign.getSide(side).getLine(1).equalsIgnoreCase(criterea)) {
                        found = true;
                        break;
                    }
                }
            } else {
                otherBlock = otherBlock.getRelative(way);
            }
        }
        if (!found) {
            return null;
        }
        return otherBlock;
    }

    public static Block getRightBlock(Block sign) {
        return sign.getRelative(getRight(sign));
    }

    /**
     * Gets the right direction from the perspective of the front of the sign.
     *
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *     if you ask about a stone block, it's considered a wall
     *     sign).
     * @return the cardinal or ordinal direction to a player's left as they face the sign to read
     *     it; if the sign is
     *     oriented in a further direction,
     *     the result is rounded to the nearest ordinal direction.
     */
    public static BlockFace getRight(Block sign) {
        return getCounterClockWise(getFront(sign));
    }

    public static Block getLeftBlock(Block sign) {

        return sign.getRelative(getLeft(sign));
    }

    /**
     * Gets the left direction from the perspective of the front of the sign.
     *
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *     if you ask about a stone block, it's considered a wall
     *     sign).
     * @return the cardinal or ordinal direction to a player's right they face the sign to read it;
     *     if the sign is
     *     oriented in a further direction, the
     *     result is rounded to the nearest ordinal direction.
     */
    public static BlockFace getLeft(Block sign) {
        return getClockWise(getFront(sign));
    }

    /**
     * Returns whether the sign is oriented along a cardinal direction.
     *
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     *     if you ask about a stone block, it's considered a wall
     *     sign).
     * @return true if the sign is oriented along a cardinal direction (or if it's a wall sign,
     *     since those are always oriented along cardinal
     *     directions); false otherwise.
     */
    public static boolean isCardinal(Block sign) {
        BlockFace facing = getFront(sign);
        return switch (facing) {
            case NORTH, SOUTH, EAST, WEST -> true;
            default -> false;
        };
    }

    /**
     * Gets the clockwise rotated face of this sign.
     *
     * @param face Start from direction
     * @return clockwise direction
     */
    public static BlockFace getClockWise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> BlockFace.SELF;
        };
    }

    /**
     * Gets the counterclockwise rotated face of this sign.
     *
     * @param face Start from direction
     * @return clockwise direction
     */
    public static BlockFace getCounterClockWise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case EAST -> BlockFace.NORTH;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            default -> BlockFace.SELF;
        };
    }

    /**
     * Cancels a sign change event, and destroys the sign in the process.
     *
     * @param event The event that is to be cancelled.
     */
    public static void cancelSignChange(SignChangeEvent event) {
        event.setCancelled(true);
        event.getBlock().breakNaturally();
    }
}
