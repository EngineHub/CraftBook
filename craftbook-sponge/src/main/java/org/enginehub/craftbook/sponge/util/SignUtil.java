/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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
package org.enginehub.craftbook.sponge.util;

import com.google.common.collect.Lists;
import org.enginehub.craftbook.core.CraftBookAPI;
import org.enginehub.craftbook.sponge.mechanics.variable.Variables;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Convenience methods for dealing with some sign block data.
 */
public final class SignUtil {

    /**
     * Gets whether or not the block at this location is a sign.
     *
     * @param block The location to check
     * @return If it is a sign
     */
    public static boolean isSign(Location<World> block) {
        if (isSign(block.getBlock())) {
            if (!block.getTileEntity().isPresent()) {
                CraftBookAPI.inst().getLogger().warn("Corrupted tile entity (Sign) at " + block.getBlockPosition() + " in world " + block.getExtent().getName());
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * If this blockstate is a sign.
     *
     * @param block The blockstate to check
     * @return If it is a sign
     */
    public static boolean isSign(BlockState block) {
        return block.getType() == BlockTypes.STANDING_SIGN || block.getType() == BlockTypes.WALL_SIGN;
    }

    public static List<Location<World>> getAttachedSigns(Location<World> block) {
        List<Location<World>> attachedSigns = Lists.newArrayList();

        for (Direction directFace : BlockUtil.getDirectFaces()) {
            if (SignUtil.isSign(block.getRelative(directFace))) {
                attachedSigns.add(block.getRelative(directFace));
            }
        }

        return attachedSigns;
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the direction a player would be facing when reading the sign; i.e. the face that is actually the back
     * side of the sign.
     */
    public static Direction getFacing(Location<World> sign) {
        return getBack(sign);
    }

    /**
     * @param sign treated as sign post if it is such, or else assumed to be a wall sign (i.e.,
     * if you ask about a stone block, it's considered a wall
     * sign).
     * @return the side of the sign containing the text (in other words, when a player places a new sign,
     * while facing north, this will return south).
     */
    public static Direction getFront(Location<World> sign) {
        Optional<Direction> data = sign.get(Keys.DIRECTION);

        return data.orElse(Direction.NONE);
    }

    public static Location<World> getFrontBlock(Location<World> sign) {
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
    public static Direction getBack(Location<World> sign) {
        Direction front = getFront(sign);
        if (front == null) return Direction.NONE;

        return front.getOpposite();
    }

    public static Location<World> getBackBlock(Location<World> sign) {
        return sign.getRelative(getBack(sign));
    }

    public static Location<World> getNextSign(Location<World> sign, String criterea, int searchRadius) {
        Location<World> otherBlock = sign;
        Direction way = getBack(sign);
        boolean found = false;
        for (int i = 0; i < searchRadius; i++) {
            if (isSign(otherBlock.getRelative(way))) {
                otherBlock = otherBlock.getRelative(way);
                if(getTextRaw((Sign) otherBlock.getTileEntity().get(), 1).equalsIgnoreCase(criterea)) {
                    found = true;
                    break;
                }
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
    public static Direction getRight(Location<World> sign) {
        Direction front = getFront(sign);
        if (front == null) return Direction.NONE;

        return getClockWise(front);
    }

    public static Location<World> getLeftBlock(Location<World> sign) {
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
    public static Direction getLeft(Location<World> sign) {
        Direction front = getFront(sign);
        if (front == null) return Direction.NONE;

        return getCounterClockWise(front);
    }

    public static Location<World> getRightBlock(Location<World> sign) {
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
                return face;
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
                return face;
        }
    }

    /* From this point on - replacements for ChangedSign in CB 3.x */

    public static String getTextRaw(Sign sign, int line) {
        return getTextRaw(sign.get(SignData.class).get(), line);
    }

    public static String getTextRaw(SignData sign, int line) {
        return getTextRaw(getText(sign, line));
    }

    public static String getTextRaw(Text text) {
        String raw = text.toPlain();
        if(Variables.instance != null)
            raw = Variables.instance.parseVariables(raw, null);
        return raw;
    }

    public static Text getText(SignData sign, int line) {
        return sign.lines().get(line);
    }
}
