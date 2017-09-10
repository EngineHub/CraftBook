/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.util;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.craftbook.core.util.RegexUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class LocationUtil {

    /**
     * Gets an Inventory from a location.
     *
     * @param location The location to look for inventories at.
     * @return An inventory, if present.
     */
    public static Optional<Inventory> getInventoryForLocation(Location location) {
        Inventory inventory = null;

        if(location.hasTileEntity()) {
            TileEntity tileEntity = (TileEntity) location.getTileEntity().get();
            if(tileEntity instanceof Carrier) {
                inventory = ((Carrier) tileEntity).getInventory();
            }
        }

        return Optional.ofNullable(inventory);
    }

    public static double getDistanceSquared(Location<World> location1, Location<World> location2) {
        if(!location1.getExtent().equals(location2.getExtent())) {
            return Integer.MAX_VALUE;
        } else {
            return location1.getPosition().distanceSquared(location2.getPosition());
        }
    }

    public static boolean isWithinSphericalRadius(Location<World> location1, Location<World> location2, double radius) {
        return location1.getExtent().equals(location2.getExtent())
                && Math.floor(getDistanceSquared(location1, location2)) <= radius * radius; // Floor for more accurate readings
    }

    public static boolean isWithinRadiusPolygon(Location<World> location1, Location<World> location2, Vector3d radius) {
        if(!location1.getExtent().equals(location2.getExtent())) return false;
        if(location2.getX() < location1.getX() + radius.getX() && location2.getX() > location1.getX() - radius.getX())
            if(location2.getY() < location1.getY() + radius.getY() && location2.getY() > location1.getY() - radius.getY())
                if(location2.getZ() < location1.getZ() + radius.getZ() && location2.getZ() > location1.getZ() - radius.getX())
                    return true;
        return false;
    }

    public static boolean isWithinRadius(Location<World> location1, Location<World> location2, Vector3d radius) {
        return radius.getX() == radius.getZ() && radius.getX() == radius.getY()
                && isWithinSphericalRadius(location1, location2, radius.getFloorX())
                || (radius.getX() != radius.getY()
                || radius.getY() != radius.getZ()
                || radius.getX() != radius.getZ())
                && isWithinRadiusPolygon(location1, location2, radius);
    }

    public static Collection<Entity> getNearbyEntities(Location<World> location, Vector3d radius) {
        int chunkRadiusX = radius.getFloorX() < 16 ? 1 : radius.getFloorX() / 16;
        int chunkRadiusZ = radius.getFloorZ() < 16 ? 1 : radius.getFloorZ() / 16;
        HashSet<Entity> radiusEntities = new HashSet<>();
        for (int chX = 0 - chunkRadiusX; chX <= chunkRadiusX; chX++) {
            for (int chZ = 0 - chunkRadiusZ; chZ <= chunkRadiusZ; chZ++) {
                int offChunkX = location.getChunkPosition().getX() + chX;
                int offChunkZ = location.getChunkPosition().getZ() + chZ;
                location.getExtent().getChunk(offChunkX, 0, offChunkZ).ifPresent(chunk -> {
                    for (Entity e : chunk.getEntities()) {
                        if (e == null || e.isRemoved()) {
                            continue;
                        }
                        if (isWithinRadius(location, e.getLocation(), radius)) {
                            radiusEntities.add(e);
                        }
                    }
                });
            }
        }
        return radiusEntities;
    }

    public static boolean isLocationWithinWorld(Location location) {
        return location.getBlockY() < location.getExtent().getBlockMax().getY() && location.getBlockY() >= location.getExtent().getBlockMin().getY();
    }

    public static Location<World> locationFromString(String string) {
        String[] parts = RegexUtil.COMMA_PATTERN.split(string);
        if(parts.length < 4)
            return null;

        World world = Sponge.getGame().getServer().getWorld(parts[0]).orElse(null);
        if(world == null)
            return null;

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);

        return world.getLocation(x, y, z);
    }

    /**
     * Gets the offset of the blocks location based on the coordiante grid.
     *
     * @param block   to get offsetfrom
     * @param offsetX to add
     * @param offsetY to add
     * @param offsetZ to add
     *
     * @return block offset by given coordinates
     */
    public static Location<World> getOffset(Location<World> block, int offsetX, int offsetY, int offsetZ) {
        return block.getExtent().getLocation(block.getX() + offsetX, block.getY() + offsetY, block.getZ() + offsetZ);
    }

    public static Location<World> getRelativeOffset(Location<World> block, int offsetX, int offsetY, int offsetZ) {
        return getRelativeOffset(SignUtil.getBackBlock(block),
                SignUtil.getFacing(block),
                offsetX, offsetY, offsetZ);
    }

    /**
     * Gets the block located relative to the signs front. That means that when the sign is attached to a block and
     * the player is looking at it it
     * will add the offsetX to left or right, offsetY is added up or down and offsetZ is added front or back.
     *
     * @param block   to get relative position from
     * @param front   to work with
     * @param offsetX amount to move left(negative) or right(positive)
     * @param offsetY amount to move up(positive) or down(negative)
     * @param offsetZ amount to move back(negative) or front(positive)
     *
     * @return block located at the relative offset position
     */
    public static Location<World> getRelativeOffset(Location<World> block, Direction front, int offsetX, int offsetY, int offsetZ) {
        Direction back;
        Direction right;
        Direction left;

        switch (front) {

            case SOUTH:
                back = Direction.NORTH;
                left = Direction.EAST;
                right = Direction.WEST;
                break;
            case WEST:
                back = Direction.EAST;
                left = Direction.SOUTH;
                right = Direction.NORTH;
                break;
            case NORTH:
                back = Direction.SOUTH;
                left = Direction.WEST;
                right = Direction.EAST;
                break;
            case EAST:
                back = Direction.WEST;
                left = Direction.NORTH;
                right = Direction.SOUTH;
                break;
            default:
                back = Direction.SOUTH;
                left = Direction.EAST;
                right = Direction.WEST;
        }

        // apply left and right offset
        if (offsetX > 0) {
            block = getRelativeBlock(block, right, offsetX);
        } else if (offsetX < 0) {
            block = getRelativeBlock(block, left, offsetX);
        }

        // apply front and back offset
        if (offsetZ > 0) {
            block = getRelativeBlock(block, front, offsetZ);
        } else if (offsetZ < 0) {
            block = getRelativeBlock(block, back, offsetZ);
        }

        // apply up and down offset
        if (offsetY > 0) {
            block = getRelativeBlock(block, Direction.UP, offsetY);
        } else if (offsetY < 0) {
            block = getRelativeBlock(block, Direction.DOWN, offsetY);
        }
        return block;
    }

    /**
     * Get relative block X that way.
     *
     * @param block The location
     * @param facing The direction
     * @param amount The amount
     *
     * @return The block
     */
    private static Location<World> getRelativeBlock(Location<World> block, Direction facing, int amount) {

        amount = Math.abs(amount);
        for (int i = 0; i < amount; i++) {
            block = block.getRelative(facing);
        }
        return block;
    }

    public static String stringFromLocation(Location<World> location) {
        return location.getExtent().getName() + ',' + location.getPosition().getX() + ',' + location.getPosition().getY() + ',' + location.getPosition().getZ();
    }
}
