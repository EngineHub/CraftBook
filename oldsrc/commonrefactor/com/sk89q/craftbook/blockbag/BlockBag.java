package com.sk89q.craftbook.blockbag;
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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a source to get blocks from and store removed ones.
 *
 * @author sk89q
 */
public abstract class BlockBag {

    /**
     * Stores a record of missing blocks.
     */
    private Map<Integer, Integer> missing = new HashMap<Integer, Integer>();

    /**
     * Sets a block.
     *
     * @param x
     * @param y
     * @param z
     * @param id
     *
     * @return
     *
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(WorldInterface w, int x, int y, int z, int id) throws BlockBagException {

        if (id == 0) { // Clearing
            int existingID = w.getId(x, y, z);

            if (existingID != 0) {
                int dropped = BlockType.getDroppedBlock(existingID);
                if (dropped == -1) { // Bedrock, etc.
                    return false;
                } else if (dropped != 0) {
                    storeBlock(dropped);
                }

                return w.setId(x, y, z, id);
            }

            return false;
        } else { // Setting
            try {
                try {
                    int existingID = w.getId(x, y, z);

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
                            return w.setId(x, y, z, id);
                        }

                        fetchBlock(id);
                        return w.setId(x, y, z, id);
                    } else if (existingID == 0) {
                        fetchBlock(id);
                        return w.setId(x, y, z, id);
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

                    return w.setId(x, y, z, id);
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
     * Sets a block.
     *
     * @param pos
     * @param id
     *
     * @return
     *
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(WorldInterface w, Vector pos, int id) throws BlockBagException {

        return setBlockID(w, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), id);
    }

    /**
     * Get a block.
     *
     * @param id
     */
    public abstract void fetchBlock(int id) throws BlockBagException;

    /**
     * Store a block.
     *
     * @param id
     */
    public abstract void storeBlock(int id) throws BlockBagException;

    /**
     * Checks to see if a block exists without removing it.
     *
     * @param id
     *
     * @return whether the block exists
     */
    public boolean peekBlock(int id) {

        try {
            fetchBlock(id);
            storeBlock(id);
            return true;
        } catch (BlockBagException e) {
            return false;
        }
    }

    /**
     * Flush any changes. This is called at the end.
     */
    public abstract void flushChanges();

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     *
     * @return
     */
    public abstract void addSourcePosition(WorldInterface w, Vector pos);

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     *
     * @return
     */
    public abstract void addSingleSourcePosition(WorldInterface w, Vector pos);

    /**
     * Return the list of missing blocks.
     *
     * @return
     */
    public Map<Integer, Integer> getMissing() {

        return missing;
    }
}
