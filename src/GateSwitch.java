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

import com.sk89q.craftbook.*;
import java.util.Set;
import java.util.HashSet;

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
     * @return
     */
    public boolean toggleClosestGate(Vector pt) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        // First, find a gate to toggle -- this doesn't really find the cloest
        // gate but rather it finds the first one in its search
        for (int x1 = x - 3; x1 <= x + 3; x1++) {
            for (int y1 = y - 3; y1 <= y + 6; y1++) {
                for (int z1 = z - 3; z1 <= z + 3; z1++) {
                    if (CraftBook.getBlockID(x, y, z) == BlockType.FENCE
                            && toggleGate(new Vector(x1, y1, z1))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Toggles a gate that was found.
     * 
     * @param pt
     * @return
     */
    private boolean toggleGate(Vector pt) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        // Find the top most fence
        for (int y1 = y + 1; y1 <= y + 12; y1++) {
            if (CraftBook.getBlockID(x, y1, z) == BlockType.FENCE) {
                y = y1;
            } else {
                break;
            }
        }

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (CraftBook.getBlockID(x, y + 1, z) == 0) {
            return false;
        }

        // Close the gate if the block below does not exist as a fence
        // block, otheriwse open the gate
        boolean close = CraftBook.getBlockID(x, y - 1, z) != BlockType.FENCE;

        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        performGateToggle(new BlockVector(x, y, z), close,
                new HashSet<BlockVector>());

        return true;
    }

    /**
     * Recursively find fences to close or open. This method will do the
     * actual 'closing' or 'opening'.
     * 
     * @param pt
     * @param close
     * @param visited
     */
    private void performGateToggle(BlockVector pt, boolean close,
            Set<BlockVector> visited) {

        // Too far away
        if (visited.size() >= 8) {
            return;
        }

        // Make sure this block is actually a fence
        if (CraftBook.getBlockID(pt) != BlockType.FENCE) {
            return;
        }

        // Don't want infinite recursion
        if (visited.contains(pt)) {
            return;
        }

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        visited.add(pt);

        // Go by to nearby blocks to see if they are fences and should be closed
        performGateToggle(new BlockVector(x - 1, y, z), close, visited);
        performGateToggle(new BlockVector(x + 1, y, z), close, visited);
        performGateToggle(new BlockVector(x, y, z - 1), close, visited);
        performGateToggle(new BlockVector(x, y, z + 1), close, visited);
        performGateToggle(new BlockVector(x - 1, y, z - 1), close, visited);
        performGateToggle(new BlockVector(x - 1, y, z + 1), close, visited);
        performGateToggle(new BlockVector(x + 1, y, z - 1), close, visited);
        performGateToggle(new BlockVector(x + 1, y, z + 1), close, visited);

        // As for our current fence, if we want to close the gate then we
        // replace air/water blocks below with fence blocks; otherwise, we want
        // to replace fence blocks below with air
        int minY = Math.max(0, y - 12);
        for (int y1 = y - 1; y1 >= minY; y1--) {
            int cur = CraftBook.getBlockID(x, y1, z);

                // Allowing water allows the use of gates as flood gates
                if (cur != BlockType.WATER
                    && cur != BlockType.STATIONARY_WATER
                    && cur != BlockType.FENCE
                    && cur != 0) {
                break;
            }

            CraftBook.setBlockID(x, y1, z, close ? BlockType.FENCE : 0);
        }
    }
}
