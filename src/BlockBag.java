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

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a source to get blocks from and store removed ones.
 *
 * @author sk89q
 */
public abstract class BlockBag {
    /**
     * Stores a record of missing blocks.
     */
    private Map<Integer,Integer> missing = new HashMap<Integer,Integer>();
    
    /**
     * Sets a block.
     *
     * @param x
     * @param y
     * @param z
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(int x, int y, int z, int id) throws BlockSourceException {
        return setBlockID(new Vector(x, y, z), id);
    }

    /**
     * Sets a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(Vector pos, int id) throws BlockSourceException {
        if (id == 0) { // Clearing
            int existingID = CraftBook.getBlockID(pos);

            if (existingID != 0) {
                int dropped = BlockType.getDroppedBlock(existingID);
                if (dropped == -1) { // Bedrock, etc.
                    return false;
                } else if (dropped != 0) {
                    storeBlock(dropped);
                }

                return CraftBook.setBlockID(pos, id);
            }

            return false;
        } else { // Setting
            try {
                try {
                    int existingID = CraftBook.getBlockID(pos);

                    if (existingID != 0 && existingID != id) {
                        int dropped = BlockType.getDroppedBlock(existingID);

                        // First store the existing block
                        if (dropped == -1) { // Bedrock, etc.
                            return false;
                        } else if (dropped != 0) {
                            storeBlock(dropped);
                        }

                        // Blocks that can't be fetched...
                        if (id == BlockType.BEDROCK
                                || id == BlockType.GOLD_ORE
                                || id == BlockType.IRON_ORE
                                || id == BlockType.COAL_ORE
                                || id == BlockType.DIAMOND_ORE
                                || id == BlockType.LEAVES
                                || id == BlockType.TNT
                                || id == BlockType.MOB_SPAWNER
                                || id == BlockType.CROPS
                                || id == BlockType.REDSTONE_ORE
                                || id == BlockType.GLOWING_REDSTONE_ORE
                                || id == BlockType.SNOW
                                || id == BlockType.LIGHTSTONE
                                || id == BlockType.PORTAL) {
                            return false;
                        }

                        // Override liquids
                        if (id == BlockType.WATER
                                || id == BlockType.STATIONARY_WATER
                                || id == BlockType.LAVA
                                || id == BlockType.STATIONARY_LAVA) {
                            return CraftBook.setBlockID(pos, id);
                        }

                        fetchBlock(id);
                        return CraftBook.setBlockID(pos, id);
                    } else if (existingID == 0) {
                        fetchBlock(id);
                        return CraftBook.setBlockID(pos, id);
                    }
                } catch (OutOfBlocksException e) {
                    // Look for cobblestone
                    if (id == BlockType.STONE) {
                        fetchBlock(BlockType.COBBLESTONE);
                    // Look for dirt
                    } else if (id == BlockType.GRASS) {
                        fetchBlock(BlockType.DIRT);
                    // Look for redstone dust
                    } else if (id == BlockType.REDSTONE_WIRE) {
                        fetchBlock(331);
                    // Look for furnace
                    } else if (id == BlockType.BURNING_FURNACE) {
                        fetchBlock(BlockType.FURNACE);
                    // Look for lit redstone torch
                    } else if (id == BlockType.REDSTONE_TORCH_OFF) {
                        fetchBlock(BlockType.REDSTONE_TORCH_ON);
                    // Look for signs
                    } else if (id == BlockType.WALL_SIGN || id == BlockType.SIGN_POST) {
                        fetchBlock(323);
                    } else {
                        throw e;
                    }

                    return CraftBook.setBlockID(pos, id);
                }
            } catch (OutOfBlocksException e) {
                int missingID = e.getID();
                
                if (missing.containsKey(missingID)) {
                    missing.put(missingID, missing.get(missingID) + 1);
                } else {
                    missing.put(missingID, 1);
                }

                throw e;
            }

            return false;
        }
    }

    /**
     * Get a block.
     *
     * @param id
     */
    public abstract void fetchBlock(int id) throws BlockSourceException;
    /**
     * Store a block.
     * 
     * @param id
     */
    public abstract void storeBlock(int id) throws BlockSourceException;
    /**
     * Flush any changes. This is called at the end.
     */
    public abstract void flushChanges();

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public abstract void addSourcePosition(Vector pos);
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public abstract void addSingleSourcePosition(Vector pos);

    /**
     * Return the list of missing blocks.
     * 
     * @return
     */
    public Map<Integer,Integer> getMissing() {
        return missing;
    }
}
