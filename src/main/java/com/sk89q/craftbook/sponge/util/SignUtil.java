package com.sk89q.craftbook.sponge.util;

import com.google.common.base.Optional;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

/**
 * Convenience methods for dealing with some sign block data.
 */
public class SignUtil {

    public static boolean isSign(Location block) {
        return block.getBlockType() == BlockTypes.STANDING_SIGN || block.getBlockType() == BlockTypes.WALL_SIGN;
    }

    public static boolean isSign(BlockState block) {
        return block.getType() == BlockTypes.STANDING_SIGN || block.getType() == BlockTypes.WALL_SIGN;
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the direction a player would be facing when reading the sign; i.e. the face that is actually the back
     * side of the sign.
     */
    public static Direction getFacing(Location sign) {

        return getBack(sign);
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the side of the sign containing the text (in other words, when a player places a new sign,
     * while facing north, this will return south).
     */
    public static Direction getFront(Location sign) {

        Optional<Direction> data = sign.get(Keys.DIRECTION);

        if (data.isPresent())
            return data.get();
        else return null;
    }

    public static Location getFrontBlock(Location sign) {

        return sign.getRelative(getFront(sign));
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the blank side of the sign opposite the text. In the case of a wall sign,
     * the block in this direction is the block to which the sign is
     * attached. This is also the direction a player would be facing when reading the sign; see {@link #getFront(Location)}.
     */
    public static Direction getBack(Location sign) {

        Direction front = getFront(sign);
        if (front == null) return null;

        return front.getOpposite();
    }

    public static Location getBackBlock(Location sign) {

        return sign.getRelative(getBack(sign));
    }

    public static Location getNextSign(Location sign, String criterea, int searchRadius) {

        Location otherBlock = sign;
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
    public static Direction getRight(Location sign) {

        Direction front = getFront(sign);
        if (front == null) return null;

        return getClockWise(front);
    }

    public static Location getLeftBlock(Location sign) {

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
    public static Direction getLeft(Location sign) {

        Direction front = getFront(sign);
        if (front == null) return null;

        return getCounterClockWise(front);
    }

    public static Location getRightBlock(Location sign) {

        return sign.getRelative(getRight(sign));
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

        Text text = getText(sign.get(SignData.class).get(), line);
        if (text instanceof Literal) return ((Literal) text).getContent();
        return text.toString();
    }

    public static String getTextRaw(SignData sign, int line) {

        Text text = getText(sign, line);
        if (text instanceof Literal) return ((Literal) text).getContent();
        return text.toString();
    }

    public static Text getText(SignData sign, int line) {

        return sign.lines().get(line);
    }
}
