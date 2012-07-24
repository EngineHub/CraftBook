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

package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.blockbag.BlockBag;
import com.sk89q.craftbook.blockbag.BlockBagException;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler for gates. Gates are merely fence blocks. When they are closed
 * or open, a nearby fence will be found, the algorithm will traverse to the
 * top-most connected fence block, and then proceed to recurse to the sides
 * up to a certain number of fences. To the fences that it gets to, it will
 * iterate over the blocks below to open or close the gate.
 *
 * @author sk89q
 */
public class GateSwitch {

    /**
     * Toggles the gate closest to a location.
     *
     * @param pt
     * @param bag
     * @param smallSearchSize
     *
     * @return
     */
    public static boolean toggleGates(WorldInterface w, Vector pt, BlockBag bag,
                                      boolean smallSearchSize) throws BlockBagException {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        boolean foundGate = false;

        Set<BlockVector> visitedColumns = new HashSet<BlockVector>();

        if (smallSearchSize) {
            // Toggle nearby gates
            for (int x1 = x - 1; x1 <= x + 1; x1++) {
                for (int y1 = y - 2; y1 <= y + 1; y1++) {
                    for (int z1 = z - 1; z1 <= z + 1; z1++) {
                        if (recurseColumn(w, new Vector(x1, y1, z1), visitedColumns,
                                null, bag)) {
                            foundGate = true;
                        }
                    }
                }
            }
        } else {
            // Toggle nearby gates
            for (int x1 = x - 3; x1 <= x + 3; x1++) {
                for (int y1 = y - 3; y1 <= y + 6; y1++) {
                    for (int z1 = z - 3; z1 <= z + 3; z1++) {
                        if (recurseColumn(w, new Vector(x1, y1, z1), visitedColumns,
                                null, bag)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        bag.flushChanges();

        return foundGate;
    }

    /**
     * Set gate states of gates closest to a location.
     *
     * @param pt
     * @param bag
     * @param close
     * @param searchSize
     *
     * @return
     */
    public static boolean setGateState(WorldInterface w, Vector pt, BlockBag bag, boolean close,
                                       boolean smallSearchSize) throws BlockBagException {

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        boolean foundGate = false;

        Set<BlockVector> visitedColumns = new HashSet<BlockVector>();

        if (smallSearchSize) {
            // Toggle nearby gates
            for (int x1 = x - 1; x1 <= x + 1; x1++) {
                for (int y1 = y - 2; y1 <= y + 1; y1++) {
                    for (int z1 = z - 1; z1 <= z + 1; z1++) {
                        if (recurseColumn(w, new Vector(x1, y1, z1), visitedColumns,
                                close, bag)) {
                            foundGate = true;
                        }
                    }
                }
            }
        } else {
            // Toggle nearby gates
            for (int x1 = x - 3; x1 <= x + 3; x1++) {
                for (int y1 = y - 3; y1 <= y + 6; y1++) {
                    for (int z1 = z - 3; z1 <= z + 3; z1++) {
                        if (recurseColumn(w, new Vector(x1, y1, z1), visitedColumns,
                                close, bag)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        bag.flushChanges();

        return foundGate;
    }

    /**
     * Toggles one column of gate.
     *
     * @param pt
     * @param visitedColumns
     * @param close
     * @param bag
     * @param state
     *
     * @return
     */
    private static boolean recurseColumn(WorldInterface w, Vector pt, Set<BlockVector> visitedColumns,
                                         Boolean close, BlockBag bag)
            throws BlockBagException {

        if (visitedColumns.size() > 14) {
            return false;
        }
        if (visitedColumns.contains(pt.setY(0).toBlockVector())) {
            return false;
        }
        if (w.getId(pt) != BlockType.FENCE) {
            return false;
        }

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        visitedColumns.add(pt.setY(0).toBlockVector());

        // Find the top most fence
        for (int y1 = y + 1; y1 <= y + 12; y1++) {
            if (w.getId(x, y1, z) == BlockType.FENCE) {
                y = y1;
            } else {
                break;
            }
        }

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (w.getId(x, y + 1, z) == 0) {
            return false;
        }

        if (close == null) {
            // Close the gate if the block below does not exist as a fence
            // block, otherwise open the gate
            close = w.getId(x, y - 1, z) != BlockType.FENCE;
        }

        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        toggleColumn(w, new BlockVector(x, y, z), close, visitedColumns, bag);

        return true;
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     *
     * @param topPoint
     * @param close
     * @param visitedColumns
     */
    private static void toggleColumn(WorldInterface w, Vector topPoint, boolean close,
                                     Set<BlockVector> visitedColumns, BlockBag bag)
            throws BlockBagException {

        int x = topPoint.getBlockX();
        int y = topPoint.getBlockY();
        int z = topPoint.getBlockZ();

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        int minY = Math.max(0, y - 12);
        for (int y1 = y - 1; y1 >= minY; y1--) {
            int cur = w.getId(x, y1, z);

            // Allowing water allows the use of gates as flood gates
            if (cur != BlockType.WATER
                    && cur != BlockType.STATIONARY_WATER
                    && cur != BlockType.LAVA
                    && cur != BlockType.STATIONARY_LAVA
                    && cur != BlockType.FENCE
                    && cur != 0) {
                break;
            }

            bag.setBlockID(w, x, y1, z, close ? BlockType.FENCE : 0);

            Vector pt = new Vector(x, y1, z);
            recurseColumn(w, pt.add(1, 0, 0), visitedColumns, close, bag);
            recurseColumn(w, pt.add(-1, 0, 0), visitedColumns, close, bag);
            recurseColumn(w, pt.add(0, 0, 1), visitedColumns, close, bag);
            recurseColumn(w, pt.add(0, 0, -1), visitedColumns, close, bag);
        }

        recurseColumn(w, topPoint.add(1, 0, 0), visitedColumns, close, bag);
        recurseColumn(w, topPoint.add(-1, 0, 0), visitedColumns, close, bag);
        recurseColumn(w, topPoint.add(0, 0, 1), visitedColumns, close, bag);
        recurseColumn(w, topPoint.add(0, 0, -1), visitedColumns, close, bag);
    }
}
