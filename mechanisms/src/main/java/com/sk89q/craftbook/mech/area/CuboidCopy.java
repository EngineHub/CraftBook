package com.sk89q.craftbook.mech.area;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cuboid copy that can be saved to disk and
 * loaded from disk. Supports multiple formats, like MCEDIT,
 * or flat file copying.
 *
 * @author Silthus
 */
public abstract class CuboidCopy {

    protected Vector origin;
    protected Vector size;
    protected int width;
    protected int height;
    protected int length;

    public CuboidCopy(Vector origin, Vector size) {

        this.origin = origin;
        this.size = size;
        this.width = size.getBlockX();
        this.height = size.getBlockY();
        this.length = size.getBlockZ();
    }

    protected CuboidCopy() {
        // used as constructor when file is loaded
    }

    /**
     * Loads a cuboid copy from the given file. This acts as a factory
     * selecting the right file type depending on the given File.
     *
     * @param file to load from
     * @return loaded CuboidCopy
     * @throws CuboidCopyException is thrown when loading error occured
     */
    public static CuboidCopy load(File file) throws CuboidCopyException {
        // we need to split off the file extenstion to check what class we need to use
        int index = file.getName().lastIndexOf('.');
        String extension = file.getName().substring(index);
        CuboidCopy copy = null;
        if (extension.equalsIgnoreCase("cbcopy")) {
            // this copies only blocks and not sign text or chest contents
            copy = new FlatCuboidCopy();
        } else if (extension.equalsIgnoreCase("schematic")) {
            // this copies all blocks including chest content and sign text
            copy = new MCEditCuboidCopy();
        }
        if (copy == null) {
            throw new MissingCuboidCopyException("The file " + file.getAbsolutePath() + " does not exist.");
        }
        try {
            copy.loadFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CuboidCopyException(e.getMessage());
        } catch (DataException e) {
            e.printStackTrace();
            throw new CuboidCopyException(e.getMessage());
        }
        // make sure that null is never returned but an exception is thrown instead
        return copy;
    }

    /**
     * Toggles the cuboid copy on or off depending on its state.
     *
     * @param world to toggle cuboid in
     */
    public void toggle(World world) {
        if (shouldClear(world)) {
            clear(world);
        } else {
            paste(world);
        }
    }

    /**
     * Clear the area.
     */
    public void clear(World w) {

        List<Vector> queued = new ArrayList<Vector>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Vector pt = origin.add(x, y, z);
                    if (BlockType.shouldPlaceLast(w.getBlockTypeIdAt(BukkitUtil.toLocation(w, pt)))) {
                        w.getBlockAt(BukkitUtil.toLocation(w, pt)).setTypeId(0);
                    } else {
                        // Can't destroy these blocks yet
                        queued.add(pt);
                    }
                }
            }
        }

        for (Vector pt : queued) {
            w.getBlockAt(BukkitUtil.toLocation(w, pt)).setTypeId(0);
        }
    }

    /**
     * Get the distance between a point and this cuboid.
     *
     * @param pos of the vector to compare
     * @return distance between cuboid and point
     */
    public double distance(Vector pos) {

        Vector max = origin.add(new Vector(width, height, length));
        int closestX = Math.max(origin.getBlockX(),
                Math.min(max.getBlockX(), pos.getBlockX()));
        int closestY = Math.max(origin.getBlockY(),
                Math.min(max.getBlockY(), pos.getBlockY()));
        int closestZ = Math.max(origin.getBlockZ(),
                Math.min(max.getBlockZ(), pos.getBlockZ()));
        return pos.distance(new Vector(closestX, closestY, closestZ));
    }

    /**
     * Checks the state of the cuboid. If it should be toggled on or off.
     *
     * @param world to check in
     * @return true if cuboid is toggled on and should be cleared
     */
    public abstract boolean shouldClear(World world);

    /**
     * Saves the cuboid to file.
     *
     * @param file to save to
     * @throws IOException
     */
    public abstract void save(File file) throws IOException, DataException;

    /**
     * Loads the cuboid from file. This method is for all sub classes.
     *
     * @param file to load from
     * @throws IOException
     * @throws CuboidCopyException
     */
    protected abstract void loadFromFile(File file) throws IOException, CuboidCopyException, DataException;

    /**
     * Pastes the cuboid copy into the world on its point of origin.
     *
     * @param world to paste into
     */
    public abstract void paste(World world);

    /**
     * Copies the cuboid from the world caching its state and blocks.
     *
     * @param world to copy from
     */
    public abstract void copy(World world);
}
