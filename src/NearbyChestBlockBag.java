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
    private Set<BagComplexBlock<Chest>> chests;

    /**
     * Construct the object.
     * 
     * @param origin
     */
    public NearbyChestBlockBag(Vector origin) {
        BagComplexBlockComparator comparator =
                new BagComplexBlockComparator(origin);
        chests = new TreeSet<BagComplexBlock<Chest>>(comparator);
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
        for (BagComplexBlock<Chest> c : chests) {
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
                        if (newAmount == 0) {
                            // Flush the changes only if we've removed an item completely. The item otherwise remains in the chest.
                            flushChanges();
                        }

                        return;
                    }
                }
            }
        }

        throw new OutOfBlocksException(id);
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
        for (BagComplexBlock<Chest> c : chests) {
            Chest chest = c.getChest();
            Item[] itemArray = chest.getContents();
            int emptySlot = -1;

            // Find an existing slot to put it into
            for (int i = 0; itemArray.length > i; i++) {
                if (itemArray[i] != null) {
                    // Found an item
                    if (itemArray[i].getItemId() == id &&
                        itemArray[i].getAmount() < 64) {
                        itemArray[i].setAmount(itemArray[i].getAmount() + 1);

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
                flushChanges(); // Just in case
                
                return;
            }
        }

        throw new OutOfSpaceException(id);
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
                            Item[] itemArray = chest.getContents();
                            boolean occupied = false;
                            
                            // Got to make sure that at least one slot is occupied
                            for (int i = 0; itemArray.length > i; i++) {
                                if (itemArray[i] != null) {
                                    // Found an item
                                    if (itemArray[i].getAmount() > 0) {
                                        occupied = true;
                                        break;
                                    }
                                }
                            }

                            if (occupied) {
                                chests.add(new BagComplexBlock<Chest>(cur.toBlockVector(), chest));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Flush changes.
     */
    public void flushChanges() {
        for (BagComplexBlock<Chest> c : chests) {
            c.getChest().update();
        }
    }
}
