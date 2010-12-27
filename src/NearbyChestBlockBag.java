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
import java.util.TreeSet;
import com.sk89q.craftbook.*;

/**
 *
 * @author sk89q
 */
public class NearbyChestBlockBag extends BlockBag {
    /**
     * List of chests.
     */
    private Set<ComparableInventory> chests;

    /**
     * Construct the object.
     * 
     * @param origin
     */
    public NearbyChestBlockBag(Vector origin) {
        DistanceComparator<ComparableInventory> comparator =
                new DistanceComparator<ComparableInventory>(origin);
        chests = new TreeSet<ComparableInventory>(comparator);
    }

    /**
     * Gets a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfBlocksException
     */
    public void fetchBlock(int id) throws BlockSourceException {
        try {
            for (ComparableInventory c : chests) {
                Inventory chest = c.getInventory();
                Item[] itemArray = chest.getContents();
                
                // Find the item
                for (int i = 0; itemArray.length > i; i++) {
                    if (itemArray[i] != null) {
                        // Found an item
                        if (itemArray[i].getItemId() == id &&
                            itemArray[i].getAmount() >= 1) {
                            int newAmount = itemArray[i].getAmount() - 1;
    
                            if (newAmount > 0) {
                                itemArray[i].setAmount(newAmount);
                            } else {
                                itemArray[i] = null;
                            }
                            
                            ItemArrayUtil.setContents((ItemArray<?>)chest, itemArray);
    
                            return;
                        }
                    }
                }
            }
    
            throw new OutOfBlocksException(id);
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
    public void storeBlock(int id) throws BlockSourceException {
        try {
            for (ComparableInventory c : chests) {
                Inventory chest = c.getInventory();
                Item[] itemArray = chest.getContents();
                int emptySlot = -1;
    
                // Find an existing slot to put it into
                for (int i = 0; itemArray.length > i; i++) {
                    if (itemArray[i] != null) {
                        // Found an item
                        if (itemArray[i].getItemId() == id &&
                            itemArray[i].getAmount() < 64) {
                            int newAmount = itemArray[i].getAmount() + 1;
                            itemArray[i].setAmount(newAmount);
                            
                            ItemArrayUtil.setContents((ItemArray<?>)chest, itemArray);
    
                            return;
                        }
                    } else {
                        emptySlot = i;
                    }
                }
    
                // Didn't find an existing stack, so let's create a new one
                if (emptySlot != -1) {
                    itemArray[emptySlot] = new Item(id, 1);
                    
                    ItemArrayUtil.setContents((ItemArray<?>)chest, itemArray);
                    
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
    public void storeBlock(int id, int amount) throws BlockSourceException {
        
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSourcePosition(Vector pos) {
        int ox = pos.getBlockX();
        int oy = pos.getBlockY();
        int oz = pos.getBlockZ();

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Vector cur = pos.add(x, y, z);
                    addSingleSourcePosition(cur);
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
    public void addSingleSourcePosition(Vector pos) {
        int x = pos.getBlockX();
        int y = pos.getBlockY();
        int z = pos.getBlockZ();
        
        if (CraftBook.getBlockID(pos) == BlockType.CHEST) {
            ComplexBlock complexBlock =
                    etc.getServer().getComplexBlock(x, y, z);

            if (complexBlock instanceof Chest) {
                Chest chest = (Chest)complexBlock;
                chests.add(new ComparableInventory(pos.toBlockVector(), chest));
            } else if (complexBlock instanceof DoubleChest) {
                DoubleChest chest = (DoubleChest)complexBlock;
                chests.add(new ComparableInventory(
                        new Vector(chest.getX(), chest.getY(), chest.getZ()), chest));
                // Double chests have two chest blocks, so creating a new Vector
                // should theoretically prevent duplication (but it doesn't
                // (yet...)
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
    public Inventory[] getInventories() {
        Inventory[] inventories = new Inventory[chests.size()];
        
        int i = 0;
        for (ComparableInventory c : chests) {
            inventories[i] = c.getInventory();
            i++;
        }
        
        return inventories;
    }

    /**
     * Flush changes.
     */
    public void flushChanges() {
        for (ComparableInventory c : chests) {
            c.getInventory().update();
        }
    }
    
    /**
     * Factory.
     * 
     * @author sk89q
     */
    public static class Factory implements BlockBagFactory {
        public BlockBag createBlockSource(Vector v) {
            return new NearbyChestBlockBag(v);
        }
    }
}
