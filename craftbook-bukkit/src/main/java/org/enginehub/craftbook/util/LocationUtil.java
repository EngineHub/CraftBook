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

import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class LocationUtil {

    private LocationUtil() {
    }

    /**
     * Gets whether checked is within a spherical radius of base.
     *
     * <p>
     *     Note: This checks <em>real</em> distance, not block distance.
     *     For block distance {@link LocationUtil#isWithinSphericalRadius(Block, Block, double)}.
     * </p>
     *
     * @param base The base location
     * @param checked The checked location
     * @param radius The radius around base to check within
     * @return If checked is within the radius
     */
    public static boolean isWithinSphericalRadius(Location base, Location checked, double radius) {
        return base.getWorld() == checked.getWorld() && base.distanceSquared(checked) <= radius * radius;
    }

    /**
     * Gets whether checked is within a spherical radius of base.
     *
     * <p>
     *     Note: This checks <em>block</em> distance, not real distance.
     *     For block distance {@link LocationUtil#isWithinSphericalRadius(Location, Location, double)}.
     * </p>
     *
     * @param base The base location
     * @param checked The checked location
     * @param radius The radius around base to check within
     * @return If checked is within the radius
     */
    public static boolean isWithinSphericalRadius(Block base, Block checked, double radius) {
        return isWithinSphericalRadius(base.getLocation(), checked.getLocation(), radius + 0.5);
    }

    public static boolean isWithinRadiusPolygon(Location l1, Location l2, Vector3 radius) {

        if (!l1.getWorld().equals(l2.getWorld())) return false;
        if (l2.getX() < l1.getX() + radius.x() && l2.getX() > l1.getX() - radius.x())
            if (l2.getY() < l1.getY() + radius.y() && l2.getY() > l1.getY() - radius.y())
                if (l2.getZ() < l1.getZ() + radius.z() && l2.getZ() > l1.getZ() - radius.z())
                    return true;
        return false;
    }

    /**
     * Passed a vector, and it smartly detects if its spherical or polygon.
     *
     * @param l1
     * @param l2
     * @param radius
     * @return
     */
    public static boolean isWithinRadius(Location l1, Location l2, Vector3 radius) {

        return radius.x() == radius.z() && radius.x() == radius.y() && isWithinSphericalRadius(l1, l2, radius.x()) || (radius.x() != radius.y() || radius.y() != radius.z() || radius.x() != radius.z()) && isWithinRadiusPolygon(l1, l2, radius);
    }

    public static Block getRelativeOffset(Block sign, int offsetX, int offsetY, int offsetZ) {

        return getRelativeOffset(SignUtil.getBackBlock(sign),
            SignUtil.getFacing(sign),
            offsetX, offsetY, offsetZ);
    }

    /**
     * Gets the block located relative to the signs front. That means that when the sign is attached
     * to a block and
     * the player is looking at it it
     * will add the offsetX to left or right, offsetY is added up or down and offsetZ is added front
     * or back.
     *
     * @param block to get relative position from
     * @param front to work with
     * @param offsetX amount to move left(negative) or right(positive)
     * @param offsetY amount to move up(positive) or down(negative)
     * @param offsetZ amount to move back(negative) or front(positive)
     * @return block located at the relative offset position
     */
    public static Block getRelativeOffset(Block block, BlockFace front, int offsetX, int offsetY, int offsetZ) {

        BlockFace back;
        BlockFace right;
        BlockFace left;

        switch (front) {

            case SOUTH:
                back = BlockFace.NORTH;
                left = BlockFace.EAST;
                right = BlockFace.WEST;
                break;
            case WEST:
                back = BlockFace.EAST;
                left = BlockFace.SOUTH;
                right = BlockFace.NORTH;
                break;
            case NORTH:
                back = BlockFace.SOUTH;
                left = BlockFace.WEST;
                right = BlockFace.EAST;
                break;
            case EAST:
                back = BlockFace.WEST;
                left = BlockFace.NORTH;
                right = BlockFace.SOUTH;
                break;
            default:
                back = BlockFace.SOUTH;
                left = BlockFace.EAST;
                right = BlockFace.WEST;
        }

        // apply left and right offset
        if (offsetX > 0) {
            block = block.getRelative(right, offsetX);
        } else if (offsetX < 0) {
            block = block.getRelative(left, offsetX);
        }

        // apply front and back offset
        if (offsetZ > 0) {
            block = block.getRelative(front, offsetZ);
        } else if (offsetZ < 0) {
            block = block.getRelative(back, offsetZ);
        }

        // apply up and down offset
        if (offsetY > 0) {
            block = block.getRelative(BlockFace.UP, offsetY);
        } else if (offsetY < 0) {
            block = block.getRelative(BlockFace.DOWN, offsetY);
        }
        return block;
    }

    /**
     * Gets next vertical free space
     *
     * @param block
     * @param direction
     * @return next air block in a direction.
     */
    public static Block getNextFreeSpace(Block block, BlockFace direction) {

        while (block.getType() != Material.AIR && block.getRelative(direction).getType() != Material.AIR) {
            if (!(block.getY() < block.getWorld().getMaxHeight())) {
                break;
            }
            block = block.getRelative(direction);
        }
        return block;
    }

    /**
     * Gets centre of the top face of the passed block.
     *
     * @param block The given block
     * @return The centre of the top
     */
    public static Location getBlockCentreTop(Block block) {
        return block.getLocation().add(0.5, 1, 0.5);
    }

    /**
     * Gets an array of {@link BlockFace} that are direct.
     *
     * @return The array of {@link BlockFace}
     */
    public static BlockFace[] getDirectFaces() {

        return new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
    }

    /**
     * Gets an array of {@link BlockFace} that are indirect.
     *
     * Note: This is only indirect along the X and Z axis due to bukkit constraints.
     *
     * @return The array of {@link BlockFace}
     */
    public static BlockFace[] getIndirectFaces() {

        return new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };
    }

    public static final double EQUALS_PRECISION = 0.0001;

    /**
     * Bukkit's Location class has serious problems with floating point precision.
     */
    public static boolean equals(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) <= EQUALS_PRECISION
            && Math.abs(a.getY() - b.getY()) <= EQUALS_PRECISION
            && Math.abs(a.getZ() - b.getZ()) <= EQUALS_PRECISION;
    }

    public static float getYawFromFace(BlockFace blockFace) {
        double dx = blockFace.getModX();
        double dz = blockFace.getModZ();

        float yaw = 0;

        if (dx != 0) {
            if (dx < 0) {
                yaw = (float) (1.5 * Math.PI);
            } else {
                yaw = (float) (0.5 * Math.PI);
            }
            yaw -= Math.atan(dz / dx);
        } else if (dz < 0) {
            yaw = (float) Math.PI;
        }

        return -yaw * 180f / (float) Math.PI;
    }
}
