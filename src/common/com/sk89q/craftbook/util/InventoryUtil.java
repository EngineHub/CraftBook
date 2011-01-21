package com.sk89q.craftbook.util;
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

import com.sk89q.craftbook.access.InventoryInterface;
import com.sk89q.craftbook.access.Item;
import com.sk89q.craftbook.blockbag.NearbyChestBlockBag;

/**
 * Inventory related functions.
 * 
 * @author sk89q
 */
public class InventoryUtil {
    /**
     * Move the contents of an inventory to a chest block bag.
     *  
     * @param minecart
     * @param bag
     */
    public static void moveItemArrayToChestBag(InventoryInterface from,
            NearbyChestBlockBag bag) {
        
        Item[] fromItems = from.getItems();
        InventoryInterface[] inventories = bag.getInventories();
        int invenIndex = 0;
        boolean changed = false;
        
        try {
            for (int cartSlot = 0; cartSlot < fromItems.length; cartSlot++) {
                Item cartItem = fromItems[cartSlot];
                
                if (cartItem == null || cartItem.count == 0) {
                    continue;
                }
                
                try {
                    for (; invenIndex < inventories.length; invenIndex++) {
                        Item[] chestItems = inventories[invenIndex].getItems();
                        
                        for (int chestSlot = 0; chestSlot < chestItems.length; chestSlot++) {
                            Item chestItem = chestItems[chestSlot];
                            
                            if (chestItem.id==0||chestItem.count==0) {
                                chestItems[chestSlot] = cartItem;
                                fromItems[cartSlot] = null;
                                setContents(inventories[invenIndex], chestItems);
                                changed = true;
                                throw new TransferredItemException();
                            } else if (chestItem.id == cartItem.id
                                    && chestItem.count < 64
                                    && chestItem.count >= 0) {
                                int spaceAvailable = 64 - chestItem.count;
                                
                                if (spaceAvailable >= cartItem.count) {
                                    chestItem = chestItem.addItems(cartItem.count);
                                    fromItems[cartSlot] = null;
                                    setContents(inventories[invenIndex], chestItems);
                                    changed = true;
                                    throw new TransferredItemException();
                                } else {
                                    cartItem = cartItem.removeItems(spaceAvailable);
                                    chestItem = new Item(chestItem.id,64);
                                    changed = true;
                                }
                            }
                        }
                    }
                    
                    throw new TargetFullException();
                } catch (TransferredItemException e) {
                }
            }
        } catch (TargetFullException e) {
        }
        
        if (changed) {
            setContents(from, fromItems);
        }
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    public static void setContents(InventoryInterface itemArray, Item[] contents) {
        int size = itemArray.getLength();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.setItem(i, new Item(0,0));
            } else {
                itemArray.setItem(i, contents[i]);
            }
        }
    }

    /**
     * Thrown when an item has been fully transferred.
     */
    private static class TransferredItemException extends Exception {
        private static final long serialVersionUID = -4125958007487924445L;
    }

    /**
     * Thrown when the target is full.
     */
    private static class TargetFullException extends Exception {
        private static final long serialVersionUID = 5408687817221722647L;
    }
}
