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
    private Set<ComparableComplexBlock<Chest>> chests;

    /**
     * Construct the object.
     * 
     * @param origin
     */
    public NearbyChestBlockBag(Vector origin) {
        ComplexBlockDistanceComparator<Chest> comparator =
                new ComplexBlockDistanceComparator<Chest>(origin);
        chests = new TreeSet<ComparableComplexBlock<Chest>>(comparator);
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
            for (ComparableComplexBlock<Chest> c : chests) {
                Chest chest = c.getChest();
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
                            
                            chest.setContents(itemArray);
    
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
            for (ComparableComplexBlock<Chest> c : chests) {
                Chest chest = c.getChest();
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
                            
                            chest.setContents(itemArray);
    
                            return;
                        }
                    } else {
                        emptySlot = i;
                    }
                }
    
                // Didn't find an existing stack, so let's create a new one
                if (emptySlot != -1) {
                    itemArray[emptySlot] = new Item(id, 1);
                    
                    chest.setContents(itemArray);
                    
                    return;
                }
            }
    
            throw new OutOfSpaceException(id);
        } finally {
            flushChanges(); 
        }
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

                    if (CraftBook.getBlockID(cur) == BlockType.CHEST) {
                        ComplexBlock complexBlock =
                                etc.getServer().getComplexBlock(ox + x, oy + y, oz + z);

                        if (complexBlock instanceof Chest) {
                            Chest chest = (Chest)complexBlock;
                            chests.add(new ComparableComplexBlock<Chest>(pos.toBlockVector(), chest));
                        }
                    }
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
                chests.add(new ComparableComplexBlock<Chest>(pos.toBlockVector(), chest));
            }
        }
    }

    /**
     * Flush changes.
     */
    public void flushChanges() {
        for (ComparableComplexBlock<Chest> c : chests) {
            c.getChest().update();
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
