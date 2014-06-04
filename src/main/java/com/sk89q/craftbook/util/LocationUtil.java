package com.sk89q.craftbook.util;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.worldedit.Vector;

/**
 * @author Silthus, Me4502
 */
public final class LocationUtil {

    public static boolean isWithinSphericalRadius(Location l1, Location l2, double radius) {

        return l1.getWorld().equals(l2.getWorld()) && Math.floor(getDistanceSquared(l1, l2)) <= radius * radius; // Floor for more accurate readings
    }

    public static boolean isWithinRadiusPolygon(Location l1, Location l2, Vector radius) {

        if(!l1.getWorld().equals(l2.getWorld())) return false;
        if(l2.getX() < l1.getX() + radius.getX() && l2.getX() > l1.getX() - radius.getX())
            if(l2.getY() < l1.getY() + radius.getY() && l2.getY() > l1.getY() - radius.getY())
                if(l2.getZ() < l1.getZ() + radius.getZ() && l2.getZ() > l1.getZ() - radius.getX())
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
    public static boolean isWithinRadius(Location l1, Location l2, Vector radius) {

        return radius.getX() == radius.getZ() && radius.getX() == radius.getY() && isWithinSphericalRadius(l1,l2,radius.getBlockX()) || (radius.getX() != radius.getY() || radius.getY() != radius.getZ() || radius.getX() != radius.getZ()) && isWithinRadiusPolygon(l1,l2,radius);
    }

    public static Entity[] getNearbyEntities(Location l, Vector radius) {

        int chunkRadiusX = radius.getBlockX() < 16 ? 1 : radius.getBlockX() / 16;
        int chunkRadiusZ = radius.getBlockZ() < 16 ? 1 : radius.getBlockZ() / 16;
        HashSet<Entity> radiusEntities = new HashSet<Entity>();
        for (int chX = 0 - chunkRadiusX; chX <= chunkRadiusX; chX++) {
            for (int chZ = 0 - chunkRadiusZ; chZ <= chunkRadiusZ; chZ++) {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                for (Entity e : new Location(l.getWorld(), x + chX * 16, y, z + chZ * 16).getChunk().getEntities()) {
                    if(e == null || e.isDead() || !e.isValid())
                        continue;
                    if(isWithinRadius(l,e.getLocation(),radius))
                        radiusEntities.add(e);
                }
            }
        }
        return radiusEntities.toArray(new Entity[radiusEntities.size()]);
    }

    /**
     * Gets the distance between two points.
     *
     * @param l1
     * @param l2
     *
     * @return
     */
    public static double getDistance(Location l1, Location l2) {

        return Math.sqrt(getDistanceSquared(l1, l2));
    }

    public static double getDistanceSquared(Location l1, Location l2) {

        if(!l1.getWorld().equals(l2.getWorld())) return Integer.MAX_VALUE;

        if (CraftBookPlugin.inst().getConfiguration().useBlockDistance)
            return getBlockDistance(l1, l2) * getBlockDistance(l1, l2);
        else return l1.distanceSquared(l2);
    }

    /**
     * Gets the greatest distance between two locations. Only takes int locations and does not check a round radius.
     *
     * @param l1 to compare
     * @param l2 to compare
     *
     * @return greatest distance
     */
    public static int getBlockDistance(Location l1, Location l2) {

        if(!l1.getWorld().equals(l2.getWorld())) return Integer.MAX_VALUE;

        int x = Math.abs(l1.getBlockX() - l2.getBlockX());
        int y = Math.abs(l1.getBlockY() - l2.getBlockY());
        int z = Math.abs(l1.getBlockZ() - l2.getBlockZ());
        if (x >= y && x >= z) return x;
        else if (y >= z) // Since x is not the largest, either y or z must be
            return y;
        else return z;
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
    public static Block getOffset(Block block, int offsetX, int offsetY, int offsetZ) {

        return block.getWorld().getBlockAt(block.getX() + offsetX, block.getY() + offsetY, block.getZ() + offsetZ);
    }

    public static Block getRelativeOffset(ChangedSign sign, int offsetX, int offsetY, int offsetZ) {

        return getRelativeOffset(SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock()),
                SignUtil.getFacing(BukkitUtil.toSign(sign).getBlock()),
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
            block = getRelativeBlock(block, BlockFace.UP, offsetY);
        } else if (offsetY < 0) {
            block = getRelativeBlock(block, BlockFace.DOWN, offsetY);
        }
        return block;
    }

    /**
     * Get relative block X that way.
     *
     * @param block
     * @param facing
     * @param amount
     *
     * @return The block
     */
    private static Block getRelativeBlock(Block block, BlockFace facing, int amount) {

        amount = Math.abs(amount);
        for (int i = 0; i < amount; i++) {
            block = block.getRelative(facing);
        }
        return block;
    }

    /**
     * Gets next vertical free space
     *
     * @param block
     * @param direction
     *
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
     * Gets centre of passed block.
     *
     * @param block
     *
     * @return Centre location
     */
    public static Location getCenterOfBlock(Block block) {

        return block.getLocation().add(0.5, 1, 0.5);
    }

    public static Player[] getNearbyPlayers(Location l, int radius) {

        int chunkRadius = radius < 16 ? 1 : radius / 16;
        HashSet<Player> radiusEntities = new HashSet<Player>();
        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                for (Entity e : new Location(l.getWorld(), x + chX * 16, y, z + chZ * 16).getChunk().getEntities()) {
                    if(!(e instanceof Player))
                        continue;
                    if (getDistanceSquared(e.getLocation(), l) <= radius * radius && e.getLocation().getBlock() != l
                            .getBlock()) {
                        radiusEntities.add((Player) e);
                    }
                }
            }
        }
        return radiusEntities.toArray(new Player[radiusEntities.size()]);
    }

    /**
     * Gets an array of {@link BlockFace} that are direct.
     * 
     * @return The array of {@link BlockFace}
     */
    public static BlockFace[] getDirectFaces() {

        return new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    }

    /**
     * Gets an array of {@link BlockFace} that are indirect.
     * 
     * Note: This is only indirect along the X and Z axis due to bukkit constraints.
     * 
     * @return The array of {@link BlockFace}
     */
    public static BlockFace[] getIndirectFaces() {

        return new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
    }
}
