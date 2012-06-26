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

import java.util.Map;
import java.util.HashMap;

import org.bukkit.World;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

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
    public boolean setBlockID(World w, int x, int y, int z, int id) throws BlockBagException {
        if (id == 0) { // Clearing
            int existingID = w.getBlockTypeIdAt(x, y, z);

            if (existingID != 0) {
                int dropped = BlockType.getBlockDrop(existingID, (byte)0).getType();
                if (dropped == -1) { // Bedrock, etc.
                    return false;
                } else if (dropped != 0) {
                    storeBlock(dropped);
                }

                return w.getBlockAt(x, y, z).setTypeId(id);
            }

            return false;
        } else { // Setting
            try {
                try {
                    int existingID = w.getBlockTypeIdAt(x, y, z);

                    if (existingID != 0 && existingID != id) {
                        int dropped = BlockType.getBlockDrop(existingID,(byte)0).getType();

                        // First store the existing block
                        if (dropped == -1) { // Bedrock, etc.
                            return false;
                        } else if (dropped != 0) {
                            storeBlock(dropped);
                        }

                        // Blocks that can't be fetched...
                        if (id == BlockID.BEDROCK
                                || id == BlockID.GOLD_ORE
                                || id == BlockID.IRON_ORE
                                || id == BlockID.COAL_ORE
                                || id == BlockID.DIAMOND_ORE
                                || id == BlockID.LEAVES
                                || id == BlockID.TNT
                                || id == BlockID.MOB_SPAWNER
                                || id == BlockID.CROPS
                                || id == BlockID.REDSTONE_ORE
                                || id == BlockID.GLOWING_REDSTONE_ORE
                                || id == BlockID.SNOW
                                || id == BlockID.LIGHTSTONE
                                || id == BlockID.PORTAL) {
                            return false;
                        }

                        // Override liquids
                        if (id == BlockID.WATER
                                || id == BlockID.STATIONARY_WATER
                                || id == BlockID.LAVA
                                || id == BlockID.STATIONARY_LAVA) {
                            return w.getBlockAt(x, y, z).setTypeId(id);
                        }

                        fetchBlock(id);
                        return w.getBlockAt(x, y, z).setTypeId(id);
                    } else if (existingID == 0) {
                        fetchBlock(id);
                        return w.getBlockAt(x, y, z).setTypeId(id);
                    }
                } catch (OutOfBlocksException e) {
                    // Look for cobblestone
                    if (id == BlockID.STONE) {
                        fetchBlock(BlockType.COBBLESTONE.getID());
                    // Look for dirt
                    } else if (id == BlockID.GRASS) {
                        fetchBlock(BlockType.DIRT.getID());
                    // Look for redstone dust
                    } else if (id == BlockID.REDSTONE_WIRE) {
                        fetchBlock(331);
                    // Look for furnace
                    } else if (id == BlockID.BURNING_FURNACE) {
                        fetchBlock(BlockType.FURNACE.getID());
                    // Look for lit redstone torch
                    } else if (id == BlockID.REDSTONE_TORCH_OFF) {
                        fetchBlock(BlockType.REDSTONE_TORCH_ON.getID());
                    // Look for signs
                    } else if (id == BlockID.WALL_SIGN || id == BlockID.SIGN_POST) {
                        fetchBlock(323);
                    } else {
                        throw e;
                    }

                    return w.getBlockAt(x, y, z).setTypeId(id);
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
     * @return
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(World w, Vector pos, int id) throws BlockBagException {
        return setBlockID(w,pos.getBlockX(),pos.getBlockY(),pos.getBlockZ(),id);
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
     * @return
     */
    public abstract void addSourcePosition(World w, Vector pos);
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public abstract void addSingleSourcePosition(World w, Vector pos);

    /**
     * Return the list of missing blocks.
     * 
     * @return
     */
    public Map<Integer,Integer> getMissing() {
        return missing;
    }
}
