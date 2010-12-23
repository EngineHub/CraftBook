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
 * Door.
 *
 * @author sk89q
 */
public class Door {
    /**
     * Direction to extend the bridge.
     */
    public enum Direction {
        NORTH_SOUTH, // X
        WEST_EAST, // Z
    }

    /**
     * What bridges can be made out of.
     */
    public static Set<Integer> allowableDoorBlocks
            = new HashSet<Integer>();
    /**
     * Max bridge length.
     */
    public static int maxDoorLength = 30;

    /**
     * Returns whether a block can be used for the bridge.
     * 
     * @param id
     * @return
     */
    private static boolean canUseBlock(int id) {
        return allowableDoorBlocks.contains(id);
    }
    
    /**
     * Returns whether the door should pass through this block (and displace
     * it if needed).
     * 
     * @param t
     * @return
     */
    private static boolean canPassThrough(int t) {
    	return t == 0 || t == BlockType.WATER || t == BlockType.STATIONARY_WATER
		        || t == BlockType.LAVA || t == BlockType.STATIONARY_LAVA
		        || t == BlockType.SNOW;
    }

    /**
     * Toggles the door closest to a location.
     *
     * @param pt
     * @param direction
     * @param bag
     * @return
     */
    public static boolean toggleDoor(Vector pt, Direction direction, 
    		boolean upwards, BlockBag bag)
            throws OperationException, BlockSourceException {
        return setDoorState(pt, direction, bag, upwards, null);
    }
    
    /**
     * Toggles the gate closest to a location.
     *
     * @param pt
     * @param direction
     * @param bag
     * @return
     */
    public static boolean setDoorState(Vector pt, Direction direction,
            BlockBag bag, boolean upwards, Boolean toOpen)
            throws OperationException, BlockSourceException {

        Vector sideDir = null;
        Vector vertDir = upwards ? new Vector(0, 1, 0) : new Vector(0, -1, 0);

        if (direction == Direction.NORTH_SOUTH) {
        	sideDir = new Vector(1, 0, 0);
        } else if(direction == Direction.WEST_EAST) {
        	sideDir = new Vector(0, 0, 1);
        }
        
        int type;
        
        type = CraftBook.getBlockID(pt.add(vertDir));

        // Check construction
        if (!canUseBlock(type)) {
            throw new OperationException("The block for the door has to be an allowed block type.");
        }
        
        if (CraftBook.getBlockID(pt.add(vertDir).add(sideDir)) != type) {
            throw new OperationException("The blocks for the door to the sides have to be the same.");
        }
        
        if (CraftBook.getBlockID(pt.add(vertDir).subtract(sideDir)) != type) {
            throw new OperationException("The blocks for the door to the sides have to be the same.");
        }
        
        // Detect whether the door needs to be opened
        if (toOpen == null) {
        	toOpen = !canPassThrough(CraftBook.getBlockID(pt.add(vertDir.multiply(2))));
        }
        
        Vector cur = pt.add(vertDir.multiply(2));
        boolean found = false;
        int dist = 0;
                
        for (int i = 0; i < maxDoorLength + 2; i++) {
            int id = CraftBook.getBlockID(cur);

            if (id == BlockType.SIGN_POST) {
                ComplexBlock cBlock = etc.getServer().getComplexBlock(
                        cur.getBlockX(), cur.getBlockY(), cur.getBlockZ());

                if (cBlock instanceof Sign) {
                    Sign sign = (Sign)cBlock;
                    String line2 = sign.getText(1);

                    if (line2.equalsIgnoreCase("[Door Up]")
                            || line2.equalsIgnoreCase("[Door Down]")
                            || line2.equalsIgnoreCase("[Door End]")) {
                        found = true;
                        dist = i - 1;
                        break;
                    }
                }
            }

            // Imprecision error?
            cur = cur.add(vertDir);
        }

        if (!found) {
            throw new OperationException("Door sign required on other side (or it was too far away).");
        }

        Vector otherSideBlockPt = pt.add(vertDir.multiply(dist + 2));
        
        if (CraftBook.getBlockID(otherSideBlockPt) != type) {
            throw new OperationException("Other side is not setup correctly (needs to be "
                    + etc.getDataSource().getItem(type) + ").");
        }
        if (CraftBook.getBlockID(otherSideBlockPt.add(sideDir)) != type) {
            throw new OperationException("Other side is not setup correctly (needs to be "
                    + etc.getDataSource().getItem(type) + ").");
        }
        if (CraftBook.getBlockID(otherSideBlockPt.subtract(sideDir)) != type) {
            throw new OperationException("Other side is not setup correctly (needs to be "
                    + etc.getDataSource().getItem(type) + ").");
        }

        if (toOpen) {
            clearColumn(pt.add(vertDir.multiply(2)), vertDir, type, dist, bag);
            clearColumn(pt.add(vertDir.multiply(2).add(sideDir)), vertDir, type, dist, bag);
            clearColumn(pt.add(vertDir.multiply(2).subtract(sideDir)), vertDir, type, dist, bag);
        } else {
            setColumn(pt.add(vertDir.multiply(2)), vertDir, type, dist, bag);
            setColumn(pt.add(vertDir.multiply(2).add(sideDir)), vertDir, type, dist, bag);
            setColumn(pt.add(vertDir.multiply(2).subtract(sideDir)), vertDir, type, dist, bag);
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
    private static void clearColumn(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 0; i < dist; i++) {
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
    private static void setColumn(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 0; i < dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (canPassThrough(t)) {
                bag.setBlockID(p, type);
            } else if (t != type) {
                break;
            }
        }
    }
}
