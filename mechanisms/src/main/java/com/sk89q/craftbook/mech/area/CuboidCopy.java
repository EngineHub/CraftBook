package com.sk89q.craftbook.mech.area;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;

/**
 * Represents a cuboid copy that can be saved to disk and loaded from disk. Supports multiple formats, like MCEDIT, or flat file copying.
 * 
 * @author Silthus
 */
public abstract class CuboidCopy {

    protected World world;
    protected Vector origin;
    protected Vector size;
    protected int width;
    protected int height;
    protected int length;

    public CuboidCopy (Vector origin, Vector size, World world) {

        this.origin = origin;
        this.size = size;
        this.world = world;
        width = size.getBlockX();
        height = size.getBlockY();
        length = size.getBlockZ();
    }

    protected CuboidCopy () {
        // used as constructor when file is loaded
    }

    /**
     * Loads a cuboid copy from the given file. This acts as a factory selecting the right file type depending on the given File.
     * 
     * @param file
     *            to load from
     * @return loaded CuboidCopy
     * @throws CuboidCopyException
     *             is thrown when loading error occured
     */
    public static CuboidCopy load (File file, World world) throws CuboidCopyException {
        // we need to split off the file extenstion to check what class we need to use
        int index = file.getName().lastIndexOf('.');
        String extension = file.getName().substring(index);
        CuboidCopy copy = null;
        if (extension.equalsIgnoreCase(".cbcopy")) {
            // this copies only blocks and not sign text or chest contents
            copy = new FlatCuboidCopy();
        } else if (extension.equalsIgnoreCase(".schematic")) {
            copy = new MCEditCuboidCopy(world);
        }
        if (copy == null) throw new CuboidCopyException("The file " + file.getAbsolutePath() + " does not exist.");
        try {
            copy.loadFromFile(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            throw new CuboidCopyException(e.getMessage());
        } catch (DataException e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            throw new CuboidCopyException(e.getMessage());
        }
        // make sure that null is never returned but an exception is thrown instead
        return copy;
    }

    /**
     * Clear the area.
     */
    public void clear () {

        if (world == null || origin == null) return;
        List<Vector> queued = new ArrayList<Vector>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Vector pt = origin.add(x, y, z);
                    if (pt == null) {
                        continue;
                    }
                    if (BlockType.shouldPlaceLast(world.getBlockTypeIdAt(BukkitUtil.toLocation(world, pt)))) {
                        Block block = world.getBlockAt(BukkitUtil.toLocation(world, pt));
                        if (block instanceof InventoryHolder) {
                            InventoryHolder holder = (InventoryHolder) block;
                            holder.getInventory().clear();
                        }
                        block.setTypeId(0);
                    } else {
                        // Can't destroy these blocks yet
                        queued.add(pt);
                    }
                }
            }
        }

        for (Vector pt : queued) {
            Block block = world.getBlockAt(BukkitUtil.toLocation(world, pt));
            if (block instanceof InventoryHolder) {

                InventoryHolder holder = (InventoryHolder) block;
                holder.getInventory().clear();
            }
            block.setTypeId(0);
        }
    }

    /**
     * Get the distance between a point and this cuboid.
     * 
     * @param pos
     *            of the vector to compare
     * @return distance between cuboid and point
     */
    public double distance (Vector pos) {

        Vector max = origin.add(new Vector(width, height, length));
        int closestX = Math.max(origin.getBlockX(), Math.min(max.getBlockX(), pos.getBlockX()));
        int closestY = Math.max(origin.getBlockY(), Math.min(max.getBlockY(), pos.getBlockY()));
        int closestZ = Math.max(origin.getBlockZ(), Math.min(max.getBlockZ(), pos.getBlockZ()));
        return pos.distance(new Vector(closestX, closestY, closestZ));
    }

    /**
     * Saves the cuboid to file.
     * 
     * @param file
     *            to save to
     * @throws IOException
     */
    protected abstract void save (File file) throws IOException, DataException;

    /**
     * Loads the cuboid from file. This method is for all sub classes.
     * 
     * @param file
     *            to load from
     * @throws IOException
     * @throws CuboidCopyException
     */
    protected abstract void loadFromFile (File file) throws IOException, CuboidCopyException, DataException;

    /**
     * Pastes the cuboid copy into the world on its point of origin.
     */
    public abstract void paste ();

    /**
     * Copies the cuboid from the world caching its state and blocks.
     */
    public abstract void copy ();
}
