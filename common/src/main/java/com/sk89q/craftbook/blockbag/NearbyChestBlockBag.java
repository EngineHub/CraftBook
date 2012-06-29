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

package com.sk89q.craftbook.blockbag;

import java.util.Set;
import java.util.TreeSet;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.util.DistanceComparator;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.OutOfBlocksException;
import com.sk89q.worldedit.bags.OutOfSpaceException;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 *
 * @author sk89q
 */
public class NearbyChestBlockBag extends BlockBag {
    /**
     * List of chests.
     */
    private Set<Chest> chests;

    /**
     * Construct the object.
     * 
     * @param origin
     */
    public NearbyChestBlockBag(Vector origin) {
        DistanceComparator<Chest> comparator =
                new DistanceComparator<Chest>(origin);
        chests = new TreeSet<Chest>(comparator);
    }

    /**
     * Gets a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfBlocksException
     */
    public void fetchBlock(int id) throws BlockBagException {
        try {
            for (Chest chest : chests) {
                ItemStack[] itemArray = chest.getInventory().getContents();
                
                // Find the item
                for (int i = 0; itemArray.length > i; i++) {
                    if (itemArray[i] != null) {
                        // Found an item
                        if (itemArray[i].getTypeId() == id &&
                            itemArray[i].getAmount() >= 1) {
                            int newAmount = itemArray[i].getAmount() - 1;
    
                            if (newAmount > 0) {
                                itemArray[i] = new ItemStack(itemArray[i].getTypeId(),newAmount);
                            } else {
                                itemArray[i] = null;
                            }
                            
                            chest.getInventory().setContents(itemArray);
    
                            return;
                        }
                    }
                }
            }
    
            throw new OutOfBlocksException();
        } finally {
            flushChanges();
        }
    }

    /**
     * Stores a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public void storeBlock(int id) throws BlockBagException {
        try {
            for (Chest chest : chests) {
                ItemStack[] itemArray = chest.getInventory().getContents();
                int emptySlot = -1;
    
                // Find an existing slot to put it into
                for (int i = 0; itemArray.length > i; i++) {
                    // Found an item
                    if (itemArray[i].getTypeId() == id &&
                        itemArray[i].getAmount() < 64) {
                        int newAmount = itemArray[i].getAmount() + 1;
                        itemArray[i] = new ItemStack(itemArray[i].getTypeId(),newAmount);
                        
                        chest.getInventory().setContents(itemArray);

                        return;
                    } else {
                        emptySlot = i;
                    }
                }
    
                // Didn't find an existing stack, so let's create a new one
                if (emptySlot != -1) {
                    itemArray[emptySlot] = new ItemStack(id, 1);
                    
                    chest.getInventory().setContents(itemArray);
                    
                    return;
                }
            }
    
            throw new OutOfSpaceException(id);
        } finally {
            flushChanges(); 
        }
    }

    /**
     * Stores a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public void storeBlock(int id, int amount) throws BlockBagException {
        
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSourcePosition(WorldVector arg0) {
        //int ox = pos.getBlockX();
        //int oy = pos.getBlockY();
        //int oz = pos.getBlockZ();

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Vector cur = arg0.add(x, y, z);
                    addSingleSourcePosition(new WorldVector(arg0.getWorld(),cur));
                }
            }
        }
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSingleSourcePosition(WorldVector arg0) {
        int x = arg0.getBlockX();
        int y = arg0.getBlockY();
        int z = arg0.getBlockZ();
        
        if (BukkitUtil.toWorld(arg0.getWorld()).getBlockAt(BukkitUtil.toLocation(arg0)).getTypeId() == BlockType.CHEST.getID()) {
            BlockState complexBlock =
        	    BukkitUtil.toWorld(arg0.getWorld()).getBlockAt(x, y, z).getState();

            if (complexBlock instanceof Chest) {
                Chest chest = (Chest)complexBlock;
                
                if(!chests.contains(chest)) chests.add((Chest)complexBlock);
            }
        }
    }
    
    /**
     * Get the number of chest blocks. A double-width chest will count has
     * two chest blocks.
     * 
     * @return
     */
    public int getChestBlockCount() {
        return chests.size();
    }
    
    /**
     * Fetch related chest inventories.
     * 
     * @return
     */
    public Chest[] getInventories() {
        return chests.toArray(new Chest[0]);
    }

    /**
     * Flush changes.
     */
    public void flushChanges() {
        //TODO - Remove because... Superfluous
    }
    
    /**
     * Factory.
     * 
     * @author sk89q
     */
    public static class Factory implements BlockBagFactory {
        public BlockBag createBlockSource(World world, Vector v) {
            return new NearbyChestBlockBag(v);
        }
    }
}
