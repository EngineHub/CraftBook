package com.sk89q.craftbook.util;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Silthus, Me4502
 */
public final class LocationUtil {

    private LocationUtil() {

    }

    public static void init() {

    }

    public static boolean isWithinRadius(Location l1, Location l2, int radius) {

        return l1.getWorld().equals(l2.getWorld()) && Math.floor(getDistanceSquared(l1,
                l2)) <= radius * radius; // Floor for more accurate readings
    }

    public static Entity[] getNearbyEntities(Location l, int radius) {

        int chunkRadius = radius < 16 ? 1 : radius / 16;
        HashSet<Entity> radiusEntities = new HashSet<Entity>();
        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                for (Entity e : new Location(l.getWorld(), x + chX * 16, y, z + chZ * 16).getChunk().getEntities()) {
                    if (e.getLocation().distanceSquared(l) <= radius * radius && e.getLocation().getBlock() != l
                            .getBlock()) {
                        radiusEntities.add(e);
                    }
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

        if (CraftBookPlugin.getInstance().getLocalConfiguration().useBlockDistance)
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
     * Gets the block located relative to the signs facing. That means that when the sign is attached to a block and
     * the player is looking at it it
     * will add the offsetX to left or right, offsetY is added up or down and offsetZ is added front or back.
     *
     * @param block   to get relative position from
     * @param facing  to work with
     * @param offsetX amount to move left(negative) or right(positive)
     * @param offsetY amount to move up(positive) or down(negative)
     * @param offsetZ amount to move back(negative) or front(positive)
     *
     * @return block located at the relative offset position
     */
    public static Block getRelativeOffset(Block block, BlockFace facing, int offsetX, int offsetY, int offsetZ) {

        BlockFace front = facing;
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
     * Gets all surrounding chunks near the given block and radius.
     *
     * @param block  to get surrounding chunks for
     * @param radius around the block
     *
     * @return chunks in the given radius
     */
    public static Set<Chunk> getSurroundingChunks(Block block, int radius) {

        Chunk chunk = block.getChunk();
        radius = radius / 16 + 1;
        Set<Chunk> chunks = new LinkedHashSet<Chunk>();
        World world = chunk.getWorld();
        int cX = chunk.getX();
        int cZ = chunk.getZ();
        for (int x = radius; x >= 0; x--) {
            for (int z = radius; z >= 0; z--) {
                chunks.add(world.getChunkAt(cX + x, cZ + z));
                chunks.add(world.getChunkAt(cX - x, cZ - z));
                chunks.add(world.getChunkAt(cX + x, cZ - z));
                chunks.add(world.getChunkAt(cX - x, cZ + z));
                chunks.add(world.getChunkAt(cX + x, cZ));
                chunks.add(world.getChunkAt(cX - x, cZ));
                chunks.add(world.getChunkAt(cX, cZ + z));
                chunks.add(world.getChunkAt(cX, cZ - z));
            }
        }
        return chunks;
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

        while (block.getTypeId() != BlockID.AIR && block.getRelative(direction).getTypeId() != BlockID.AIR) {
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

    public static List<Player> getNearbyPlayers(Block block, int radius) {

        List<Player> players = new ArrayList<Player>();
        for (Chunk chunk : getSurroundingChunks(block, radius)) {
            for (Entity e : chunk.getEntities()) {
                if (e instanceof Player) {
                    players.add((Player) e);
                }
            }
        }
        return players;
    }
}
