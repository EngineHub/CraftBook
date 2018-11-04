// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.Comparator;

/**
 * Used to compare ComplexBlocks based on distance to a point.
 *
 * @author sk89q
 */
public class DistanceComparator<T extends BlockState> implements Comparator<T> {

    /**
     * Origin to compare from.
     */
    private final Vector3 origin;

    /**
     * Construct the object.
     *
     * @param origin
     */
    public DistanceComparator(Vector3 origin) {
        this.origin = origin;
    }

    /**
     * Compares two objects.
     *
     * @param o1
     * @param o2
     *
     * @return
     */
    @Override
    public int compare(T o1, T o2) {

        Block b1 = o1.getBlock();
        Block b2 = o2.getBlock();

        double dist1 = LocationUtil.getDistanceSquared(b1.getLocation(), BukkitAdapter.adapt(b1.getWorld(), origin));
        double dist2 = LocationUtil.getDistanceSquared(b2.getLocation(), BukkitAdapter.adapt(b2.getWorld(), origin));

        if (dist1 < dist2) return -1;
        else if (dist1 > dist2) return 1;
        else return 0;
    }
}
