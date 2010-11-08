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

import com.sk89q.craftbook.OperationException;
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
     * Toggles the gate closest to a location.
     *
     * @param pt
     * @param direction
     * @param bag
     * @return
     */
    public boolean toggleBridge(Vector pt, Direction direction, BlockBag bag)
            throws OperationException, BlockBagException {
        Vector change = null;
        Vector leftSide = null;
        Vector rightSide = null;

        if (direction == Direction.NORTH) {
            change = new Vector(-1, 0, 0);
            leftSide = pt.add(0, -1, -1);
            rightSide = pt.add(0, -1, 1);
        } else if(direction == Direction.SOUTH) {
            change = new Vector(1, 0, 0);
            leftSide = pt.add(0, -1, -1);
            rightSide = pt.add(0, -1, 1);
        } else if(direction == Direction.WEST) {
            change = new Vector(0, 0, 1);
            leftSide = pt.add(1, -1, 0);
            rightSide = pt.add(-1, -1, 0);
        } else if(direction == Direction.EAST) {
            change = new Vector(0, 0, -1);
            leftSide = pt.add(1, -1, 0);
            rightSide = pt.add(-1, -1, 0);
        }

        int type = CraftBook.getBlockID(pt.add(0, -1, 0));

        if (type != BlockType.WOOD) {
            throw new OperationException("The block underneath the sign has to be wood.");
        }
        if (CraftBook.getBlockID(leftSide) != BlockType.WOOD) {
            throw new OperationException("The blocks underneath the sign to the sides have to be wood.");
        }
        if (CraftBook.getBlockID(rightSide) != BlockType.WOOD) {
            throw new OperationException("The blocks underneath the sign to the sides have to be wood.");
        }

        Vector cur = pt.add(change);
        boolean found = false;
        int dist = 0;
                
        for (int i = 0; i < 30; i++) {
            int id = CraftBook.getBlockID(cur);

            if (id == BlockType.SIGN_POST) {
                ComplexBlock cBlock = etc.getServer().getComplexBlock(
                        cur.getBlockX(), cur.getBlockY(), cur.getBlockZ());

                if (cBlock instanceof Sign) {
                    Sign sign = (Sign)cBlock;
                    String line2 = sign.getText(1);

                    if (line2.equalsIgnoreCase("[Bridge]")) {
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
        if (CraftBook.getBlockID(pt.add(shift).add(0, -1, 0)) != BlockType.WOOD) {
            throw new OperationException("The other side is not setup correctly.");
        }
        if (CraftBook.getBlockID(leftSide.add(shift)) != BlockType.WOOD) {
            throw new OperationException("The other side is not setup correctly.");
        }
        if (CraftBook.getBlockID(rightSide.add(shift)) != BlockType.WOOD) {
            throw new OperationException("The other side is not setup correctly.");
        }

        boolean toOpen = CraftBook.getBlockID(pt.add(change).add(0, -1, 0)) != 0;

        if (toOpen) {
            clearRow(leftSide, change, dist, bag);
            clearRow(pt.add(0, -1, 0), change, dist, bag);
            clearRow(rightSide, change, dist, bag);
        } else {
            setRow(leftSide, change, dist, bag);
            setRow(pt.add(0, -1, 0), change, dist, bag);
            setRow(rightSide, change, dist, bag);
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
    private void clearRow(Vector origin, Vector change, int dist, BlockBag bag)
            throws BlockBagException {
        for (int i = 1; i <= dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (t == BlockType.WOOD) {
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
    private void setRow(Vector origin, Vector change, int dist, BlockBag bag)
            throws BlockBagException {
        for (int i = 1; i <= dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (t == 0) {
                bag.setBlockID(p, BlockType.WOOD);
            } else if (t != BlockType.WOOD) {
                break;
            }
        }
    }
}
