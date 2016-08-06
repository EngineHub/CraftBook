/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.DataException;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;

public abstract class CuboidCopy {

    protected World world;
    Vector origin;
    protected Vector size;
    int width;
    int height;
    protected int length;

    CuboidCopy(Vector origin, Vector size, World world) {

        this.origin = origin;
        this.size = size;
        this.world = world;
        width = size.getBlockX();
        height = size.getBlockY();
        length = size.getBlockZ();
    }

    CuboidCopy() {
        // used as constructor when file is loaded
    }

    /**
     * Loads a cuboid copy from the given file. This acts as a factory selecting the right file type depending on the
     * given File.
     *
     * @param file to load from
     *
     * @return loaded CuboidCopy
     *
     * @throws CuboidCopyException is thrown when loading error occured
     */
    public static CuboidCopy load(File file, World world) throws CuboidCopyException {
        // we need to split off the file extenstion to check what class we need to use
        String extension = file.getName().substring(file.getName().lastIndexOf('.'));
        CuboidCopy copy = null;
        if (extension.equalsIgnoreCase(".schematic")) {
            copy = new MCEditCuboidCopy(world);
        }
        if (copy == null) throw new CuboidCopyException("The file " + file.getAbsolutePath() + " does not exist.");
        try {
            copy.loadFromFile(file);
        } catch (IOException | DataException e) {
            CraftBookAPI.<CraftBookPlugin>inst().getLogger().warn("Failed to load cuboid region: " + file.getAbsolutePath(), e);
            throw new CuboidCopyException(e.getMessage());
        }
        // make sure that null is never returned but an exception is thrown instead
        return copy;
    }

    /**
     * Saves the cuboid to file.
     *
     * @param file to save to
     *
     * @throws IOException Thrown if there's an issue accessing the file
     * @throws DataException Thrown if the data in the file is invalid
     */
    protected abstract void save(File file) throws IOException, DataException;

    /**
     * Loads the cuboid from file. This method is for all sub classes.
     *
     * @param file to load from
     *
     * @throws IOException Thrown if there's an issue accessing the file
     * @throws CuboidCopyException Thrown if the file is invalid
     * @throws DataException Thrown if the file is invalid
     */
    protected abstract void loadFromFile(File file) throws IOException, CuboidCopyException, DataException;

    /**
     * Pastes the cuboid copy into the world on its point of origin.
     */
    public abstract void paste();

    /**
     * Clear the area.
     */
    public abstract void clear();

    /**
     * Copies the cuboid from the world caching its state and blocks.
     */
    public abstract void copy();
}