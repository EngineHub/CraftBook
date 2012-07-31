package com.sk89q.craftbook.mech.area;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a copy of a cuboid.
 *
 * @author sk89q
 */
public class CuboidCopy {

	private static Map<Vector, Boolean> toggledOn = new HashMap<Vector, Boolean>();

    private Vector origin;
    private int width;
    private int height;
    private int length;
	private CuboidClipboard clipboard;

    /**
     * Construct the object. This is to create a new copy at a certain
     * location.
     *
     * @param origin
     * @param size
     */
    public CuboidCopy(Vector origin, Vector size) {

        this.origin = origin;
	    this.clipboard = new CuboidClipboard(size, origin);
        width = size.getBlockX();
	    height = size.getBlockY();
	    length = size.getBlockZ();
	    trackToggleState();
    }

    /**
     * Used to create a copy when loaded from file.
     */
    private CuboidCopy() {
	    trackToggleState();
    }

    /**
     * Save the copy to file.
     *
     * @param file
     * @throws IOException
     */
    public void save(File file) throws IOException, DataException {

	    SchematicFormat.MCEDIT.save(clipboard, file);
    }

    /**
     * Save the copy to a file.
     *
     * @param path
     *
     * @throws IOException
     */
    public void save(String path) throws IOException, DataException {

        save(new File(path));
    }

    /**
     * Load a copy.
     *
     * @param file
     *
     * @return
     *
     * @throws IOException
     * @throws CuboidCopyException
     */
    public static CuboidCopy load(File file) throws IOException, CuboidCopyException, DataException {

	    CuboidClipboard clipboard = SchematicFormat.MCEDIT.load(file);

	    CuboidCopy copy = new CuboidCopy();
	    copy.clipboard = clipboard;
	    copy.origin = clipboard.getOrigin();
	    copy.width = clipboard.getWidth();
	    copy.height = clipboard.getHeight();
	    copy.length = clipboard.getLength();

	    return copy;
    }

    /**
     * Load a copy from a file.
     *
     * @param path
     *
     * @return
     *
     * @throws IOException
     * @throws CuboidCopyException
     */
    public static CuboidCopy load(String path) throws IOException, CuboidCopyException, DataException {

        return load(new File(path));
    }

    /**
     * Make the copy from world.
     */
    public void copy(EditSession session) {
	    // make a real copy with all data
	    clipboard.copy(session);
    }

    /**
     * Paste to world.
     */
    public void paste(EditSession session) throws MaxChangedBlocksException {

        clipboard.paste(session, origin, false);
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
     * Toggles the area.
     *
     * @return
     */
    public void toggle(EditSession session) throws MaxChangedBlocksException {

	    World w = Bukkit.getWorld(session.getWorld().getName());
        if (shouldClear()) {
            clear(w);
	        toggledOn.put(origin, false);
        } else {
            paste(session);
	        toggledOn.put(origin, true);
        }
    }

    /**
     * Returns true if the bridge should be turned 'off'.
     *
     * @return
     */
    public boolean shouldClear() {
	    return false;
	    /*
	    if (!toggledOn.containsKey(origin)) {
		    return true;
	    }
	    return toggledOn.get(origin);
	    */
    }

	private void trackToggleState() {
		if (!toggledOn.containsKey(origin)) {
			toggledOn.put(origin, true);
		}
	}

    /**
     * Get the distance between a point and this cuboid.
     *
     * @param pos
     *
     * @return
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
}