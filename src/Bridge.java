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

import java.util.Set;
import java.util.HashSet;
import com.sk89q.craftbook.*;

/**
 * Bridge.
 *
 * @author sk89q
 */
public class Bridge {
    /**
     * Direction to extend the bridge.
     */
    public enum Direction {
        NORTH, // -X
        SOUTH, // +X
        WEST, // +Z
        EAST, // -Z
    }

    /**
     * What bridges can be made out of.
     */
    public static Set<Integer> allowableBridgeBlocks
            = new HashSet<Integer>();
    /**
     * Max bridge length.
     */
    public static int maxBridgeLength = 30;

    /**
     * Returns whether a block can be used for the bridge.
     * 
     * @param id
     * @return
     */
    private static boolean canUseBlock(int id) {
        return allowableBridgeBlocks.contains(id);
    }

    /**
     * Toggles the gate closest to a location.
     *
     * @param pt
     * @param direction
     * @param bag
     * @return
     */
    public static boolean toggleBridge(Vector pt, Direction direction, BlockBag bag)
            throws OperationException, BlockSourceException {
        return setBridgeState(pt, direction, bag, null);
    }
    
    /**
     * Toggles the gate closest to a location.
     *
     * @param pt
     * @param direction
     * @param bag
     * @return
     */
    public static boolean setBridgeState(Vector pt, Direction direction,
            BlockBag bag, Boolean toOpen)
            throws OperationException, BlockSourceException {

        Vector change = null;
        Vector leftSide = null;
        Vector rightSide = null;
        int centerShift = 1;

        if (direction == Direction.NORTH) {
            change = new Vector(-1, 0, 0);
            leftSide = pt.add(0, 1, -1);
            rightSide = pt.add(0, 1, 1);
        } else if(direction == Direction.SOUTH) {
            change = new Vector(1, 0, 0);
            leftSide = pt.add(0, 1, -1);
            rightSide = pt.add(0, 1, 1);
        } else if(direction == Direction.WEST) {
            change = new Vector(0, 0, 1);
            leftSide = pt.add(1, 1, 0);
            rightSide = pt.add(-1, 1, 0);
        } else if(direction == Direction.EAST) {
            change = new Vector(0, 0, -1);
            leftSide = pt.add(1, 1, 0);
            rightSide = pt.add(-1, 1, 0);
        }

        int type;

        // Maybe the sign is below...
        try {
            type = CraftBook.getBlockID(pt.add(0, 1, 0));

            if (type == 0) {
                throw new OperationException("Sign not below"); // Nope
            }

            if (!canUseBlock(type)) {
                throw new OperationException("The block above the sign has to be an allowed block type.");
            }
            if (CraftBook.getBlockID(leftSide) != type) {
                throw new OperationException("The blocks above the sign to the sides have to be the same.");
            }
            if (CraftBook.getBlockID(rightSide) != type) {
                throw new OperationException("The blocks above the sign to the sides have to be the same.");
            }

        // Maybe the sign is above...
        } catch (OperationException e) {
            leftSide = leftSide.add(0, -2, 0);
            rightSide = rightSide.add(0, -2, 0);
            centerShift = -1;

            type = CraftBook.getBlockID(pt.add(0, -1, 0));

            if (!canUseBlock(type)) {
                throw new OperationException("The block below the sign has to be an allowed block type.");
            }
            if (CraftBook.getBlockID(leftSide) != type) {
                throw new OperationException("The blocks below the sign to the sides have to be the same.");
            }
            if (CraftBook.getBlockID(rightSide) != type) {
                throw new OperationException("The blocks below the sign to the sides have to be the same.");
            }
        }

        Vector cur = pt.add(change);
        boolean found = false;
        int dist = 0;
                
        for (int i = 0; i < maxBridgeLength; i++) {
            int id = CraftBook.getBlockID(cur);

            if (id == BlockType.SIGN_POST) {
                ComplexBlock cBlock = etc.getServer().getComplexBlock(
                        cur.getBlockX(), cur.getBlockY(), cur.getBlockZ());

                if (cBlock instanceof Sign) {
                    Sign sign = (Sign)cBlock;
                    String line2 = sign.getText(1);

                    if (line2.equalsIgnoreCase("[Bridge]")
                            || line2.equalsIgnoreCase("[Bridge End]")) {
                        found = true;
                        dist = i;
                        break;
                    }
                }
            }

            // Imprecision error?
            cur = cur.add(change);
        }

        if (!found) {
            throw new OperationException("[Bridge] sign required on other side (or it was too far away).");
        }

        Vector shift = change.multiply(dist + 1);
        if (CraftBook.getBlockID(pt.add(shift).add(0, centerShift, 0)) != type) {
            throw new OperationException("Other side is not setup correctly (needs to be "
                    + etc.getDataSource().getItem(type) + ").");
        }
        if (CraftBook.getBlockID(leftSide.add(shift)) != type) {
            throw new OperationException("Other side is not setup correctly (needs to be "
                    + etc.getDataSource().getItem(type) + ").");
        }
        if (CraftBook.getBlockID(rightSide.add(shift)) != type) {
            throw new OperationException("Other side is not setup correctly (needs to be "
                    + etc.getDataSource().getItem(type) + ").");
        }

        if (toOpen == null) {
            int existing = CraftBook.getBlockID(pt.add(change).add(0, centerShift, 0));
            toOpen = existing != 0
                    && existing != BlockType.WATER
                     && existing != BlockType.STATIONARY_WATER
                     && existing != BlockType.LAVA
                     && existing != BlockType.STATIONARY_LAVA;
        }

        if (toOpen) {
            clearRow(leftSide, change, type, dist, bag);
            clearRow(pt.add(0, centerShift, 0), change, type, dist, bag);
            clearRow(rightSide, change, type, dist, bag);
        } else {
            setRow(leftSide, change, type, dist, bag);
            setRow(pt.add(0, centerShift, 0), change, type, dist, bag);
            setRow(rightSide, change, type, dist, bag);
        }

        bag.flushChanges();
        
        return true;
    }

    /**
     * Clears a row.
     *
     * @param origin
     * @param change
     * @param dist
     */
    private static void clearRow(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 1; i <= dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (t == type) {
                bag.setBlockID(p, 0);
            } else if (t != 0) {
                break;
            }
        }
    }

    /**
     * Clears a row.
     *
     * @param origin
     * @param change
     * @param dist
     */
    private static void setRow(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 1; i <= dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (t == 0 || t == BlockType.WATER || t == BlockType.STATIONARY_WATER
                     || t == BlockType.LAVA || t == BlockType.STATIONARY_LAVA) {
                bag.setBlockID(p, type);
            } else if (t != type) {
                break;
            }
        }
    }
}
