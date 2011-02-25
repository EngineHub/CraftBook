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

package com.sk89q.craftbook.util;

import org.bukkit.World;
import com.sk89q.worldedit.Vector;

/**
 * A vector with a world component.
 * 
 * @author sk89q
 */
public class WorldVector extends Vector {
    /**
     * Represents the world.
     */
    protected World world; 

    /**
     * Construct the Vector object.
     *
     * @param world 
     * @param x
     * @param y
     * @param z
     */
    public WorldVector(World world, double x, double y, double z) {
        super(x, y, z);
        this.world = world;
    }

    /**
     * Construct the Vector object.
     *
     * @param world 
     * @param x
     * @param y
     * @param z
     */
    public WorldVector(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = world;
    }

    /**
     * Construct the Vector object.
     *
     * @param world 
     * @param x
     * @param y
     * @param z
     */
    public WorldVector(World world, float x, float y, float z) {
        super(x, y, z);
        this.world = world;
    }

    /**
     * Construct the Vector object.
     *
     * @param world 
     * @param pt
     */
    public WorldVector(World world, Vector pt) {
        super(pt);
        this.world = world;
    }

    /**
     * Construct the Vector object.
     * 
     * @param world 
     */
    public WorldVector(World world) {
        super();
        this.world = world;
    }
    
    /**
     * Get the world.
     * 
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get a block point from a point.
     * 
     * @param world 
     * @param x
     * @param y
     * @param z
     * @return point
     */
    public static WorldVector toBlockPoint(World world, double x, double y,
            double z) {
        return new WorldVector(world, (int)Math.floor(x),
                 (int)Math.floor(y),
                 (int)Math.floor(z));
    }

    /**
     * Gets a BlockVector version.
     * 
     * @return BlockWorldVector
     */
    public BlockWorldVector toWorldBlockVector() {
        return new BlockWorldVector(this);
    }
}
