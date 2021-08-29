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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitCraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;

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
        if (l2.getX() < l1.getX() + radius.getX() && l2.getX() > l1.getX() - radius.getX())
            if (l2.getY() < l1.getY() + radius.getY() && l2.getY() > l1.getY() - radius.getY())
                if (l2.getZ() < l1.getZ() + radius.getZ() && l2.getZ() > l1.getZ() - radius.getX())
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

        return radius.getX() == radius.getZ() && radius.getX() == radius.getY() && isWithinSphericalRadius(l1, l2, radius.getX()) || (radius.getX() != radius.getY() || radius.getY() != radius.getZ() || radius.getX() != radius.getZ()) && isWithinRadiusPolygon(l1, l2, radius);
    }

    public static Block getRelativeOffset(ChangedSign sign, int offsetX, int offsetY, int offsetZ) {

        return getRelativeOffset(SignUtil.getBackBlock(CraftBookBukkitUtil.toSign(sign).getBlock()),
            SignUtil.getFacing(CraftBookBukkitUtil.toSign(sign).getBlock()),
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


    /**
     * Teleports the vehicle the player is in to the given destination.
     * Player is ejected out of the vehicle prior to teleportation,
     * otherwise it doesn't work.
     *
     * @param player The player that will be ejected and whose vehicle will be teleported.
     * @param newLocation The location the vehicle will be teleported to.
     * @return The {@link Entity} the player was in or null if player was not in vehicle.
     */
    public static Entity ejectAndTeleportPlayerVehicle(CraftBookPlayer player, Location newLocation) {

        Player bukkitPlayer = ((BukkitCraftBookPlayer) player).getPlayer();

        if (bukkitPlayer == null || !bukkitPlayer.isInsideVehicle())
            return null;

        Entity vehicle = bukkitPlayer.getVehicle();

        if (vehicle == null)
            return null;

        newLocation.setYaw(vehicle.getLocation().getYaw());
        newLocation.setPitch(vehicle.getLocation().getPitch());

        // Vehicle must eject the passenger first,
        // otherwise vehicle.teleport() will not have any effect.
        vehicle.eject();
        vehicle.teleport(newLocation);
        return vehicle;
    }


    /**
     * Adds the player to the vehicle. Execution is delayed
     * by six ticks through a {@link BukkitRunnable} because
     * it doesn't work otherwise.
     *
     * @param vehicle The vehicle that will set the player as a passenger.
     * @param player The player that will be put inside the provided vehicle.
     */
    public static void addVehiclePassengerDelayed(Entity vehicle, CraftBookPlayer player) {

        Player bukkitPlayer = ((BukkitCraftBookPlayer) player).getPlayer();

        if (bukkitPlayer == null || vehicle == null)
            return;

        // The runnableDelayInTicks = 6 was the lowest number that
        // worked reliably across several tests.
        long runnableDelayInTicks = 6;

        // vehicle.teleport() seems to have a delay. Calling vehicle.setPassenger()
        // without the delayed runnable will not set the passenger.
        new BukkitRunnable() {
            @Override
            public void run() {
                vehicle.addPassenger(bukkitPlayer);
            }
        }.runTaskLater(CraftBookPlugin.inst(), runnableDelayInTicks);
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
