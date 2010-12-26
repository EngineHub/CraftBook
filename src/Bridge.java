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
public class Bridge extends SignOrientedMechanism {
    /**
     * Direction to extend the bridge.
     */
    private enum Direction {
        NORTH, // -X
        SOUTH, // +X
        WEST, // +Z
        EAST, // -Z
    }
    
    /**
     * What bridges can be made out of.
     */
    public static Set<Integer> allowedBlocks
            = new HashSet<Integer>();
    /**
     * Max bridge length.
     */
    public static int maxLength = 30;
    
    /**
     * Construct the instance.
     * 
     * @param pt
     * @param signText
     * @param copyManager
     */
    public Bridge(Vector pt) {
        super(pt);
    }

    /**
     * Returns whether a block can be used for the bridge.
     * 
     * @param id
     * @return
     */
    private static boolean canUseBlock(int id) {
        return allowedBlocks.contains(id);
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
     * Returns the direction of the bridge to open towards.
     * 
     * @return
     * @throws InvalidDirection
     */
    private Direction getDirection() throws InvalidDirectionException {
        int data = CraftBook.getBlockData(pt);
        
        if (data == 0x0) {
            return Bridge.Direction.EAST;
        } else if (data == 0x4) {
            return Bridge.Direction.SOUTH;
        } else if (data == 0x8) {
            return Bridge.Direction.WEST;
        } else if (data == 0xC) {
            return Bridge.Direction.NORTH; 
        } else {
            throw new InvalidDirectionException();
        }
    }

    /**
     * Toggles the bridge closest to a location.
     *
     * @param player
     * @param bag
     * @return
     */
    public void playerToggleBridge(CraftBookPlayer player, BlockBag bag)
            throws BlockSourceException {
        try {
            setState(bag, null);
        } catch (InvalidDirectionException e) {
            player.printError("The sign is not oriented at a right angle.");
        } catch (UnacceptableTypeException e) {
            player.printError("The bridge is not made from an permitted material.");
        } catch (InvalidConstructionException e) {
            player.printError(e.getMessage());
        }
    }
    
    /**
     * Sets the bridge to be active.
     * 
     * @param bag
     */
    public void setActive(BlockBag bag) {
        try {
            setState(bag, false);
        } catch (InvalidDirectionException e) {
        } catch (UnacceptableTypeException e) {
        } catch (InvalidConstructionException e) {
        } catch (BlockSourceException e) {
        }
    }
    
    /**
     * Sets the bridge to be active.
     * 
     * @param bag
     */
    public void setInactive(BlockBag bag) {
        try {
            setState(bag, true);
        } catch (InvalidDirectionException e) {
        } catch (UnacceptableTypeException e) {
        } catch (InvalidConstructionException e) {
        } catch (BlockSourceException e) {
        }
    }
    
    /**
     * Toggles the gate closest to a location.
     *
     * @param bag
     * @param toOpen
     * @return
     */
    private boolean setState(BlockBag bag, Boolean toOpen)
            throws BlockSourceException, InvalidDirectionException,
            UnacceptableTypeException, InvalidConstructionException {
        
        Direction direction = getDirection();

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

        // Block above the sign
        int type = CraftBook.getBlockID(pt.add(0, 1, 0));

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        if (type == 0
                || !canUseBlock(type)
                || CraftBook.getBlockID(leftSide) != type
                || CraftBook.getBlockID(rightSide) != type) {
            
            // The bridge is not above, so let's try below
            leftSide = leftSide.add(0, -2, 0);
            rightSide = rightSide.add(0, -2, 0);
            centerShift = -1;

            // Block below the sign
            type = CraftBook.getBlockID(pt.add(0, -1, 0));
            
            if (!canUseBlock(type)) {
                throw new UnacceptableTypeException();
            }

            // Guess not
            if (CraftBook.getBlockID(leftSide) != type
                    || CraftBook.getBlockID(rightSide) != type) {
                throw new InvalidConstructionException(
                        "Blocks adjacent to the bridge block must be of the same type.");
            }
        }

        Vector cur = pt.add(change);
        boolean found = false;
        int dist = 0;
        
        // Find the other side
        for (int i = 0; i < maxLength + 2; i++) {
            int id = CraftBook.getBlockID(cur);

            if (id == BlockType.SIGN_POST) {
                SignText otherSignText = CraftBook.getSignText(cur);
                
                if (otherSignText != null) {
                    String line2 = otherSignText.getLine2();
                    
                    if (line2.equalsIgnoreCase("[Bridge]")
                            || line2.equalsIgnoreCase("[Bridge End]")) {
                        found = true;
                        dist = i;
                        break;
                    }
                }
            }

            cur = cur.add(change);
        }

        // Failed to find the other side!
        if (!found) {
            throw new InvalidConstructionException(
                    "[Bridge] sign required on other side (or it was too far away).");
        }

        Vector shift = change.multiply(dist + 1);
        
        // Check the other side to see if it's built correctly
        if (CraftBook.getBlockID(pt.add(shift).add(0, centerShift, 0)) != type
                || CraftBook.getBlockID(leftSide.add(shift)) != type
                || CraftBook.getBlockID(rightSide.add(shift)) != type) {
            throw new InvalidConstructionException(
                    "The other side must be made with the same blocks.");
        }

        // Figure out whether the bridge needs to be opened or closed
        if (toOpen == null) {
            int existing = CraftBook.getBlockID(pt.add(change).add(0, centerShift, 0));
            toOpen = !canPassThrough(existing);
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
        
        return true;
    }

    /**
     * Clears a row.
     *
     * @param origin
     * @param change
     * @param dist
     */
    private void clearRow(Vector origin, Vector change, int type, int dist, BlockBag bag)
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
    private void setRow(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 1; i <= dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (canPassThrough(t)) {
                bag.setBlockID(p, type);
            } else if (t != type) {
                break;
            }
        }
    }
    
    /**
     * Validates the sign's environment.
     * 
     * @param signText
     * @return false to deny
     */
    public static boolean validateEnvironment(CraftBookPlayer player,
            Vector pt, SignText signText) {
        
        signText.setLine2("[Bridge]");
        
        player.print("Bridge created!");
        
        return true;
    }
    
    /**
     * Thrown when the sign is an invalid direction.
     */
    private static class InvalidDirectionException extends Exception {
        private static final long serialVersionUID = -3183606604247616362L;
    }
    
    /**
     * Thrown when the bridge type is unacceptable.
     */
    private static class UnacceptableTypeException extends Exception {
        private static final long serialVersionUID = 8340723004466483212L;
    }
    
    /**
     * Thrown when the bridge type is not constructed correctly.
     */
    private static class InvalidConstructionException extends Exception {
        private static final long serialVersionUID = 4943494589521864491L;

        /**
         * Construct the object.
         * 
         * @param msg
         */
        public InvalidConstructionException(String msg) {
            super(msg);
        }
    }
}
