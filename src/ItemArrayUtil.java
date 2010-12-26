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

/**
 * Inventory related functions.
 * 
 * @author sk89q
 */
public class ItemArrayUtil {
    /**
     * Move the contents of an inventory to a chest block bag.
     * 
     * @param minecart
     * @param bag
     */
    public static void moveItemArrayToChestBag(ItemArray<?> from,
            NearbyChestBlockBag bag) {
        
        Item[] fromItems = from.getContents();
        Inventory[] inventories = bag.getInventories();
        int invenIndex = 0;
        boolean changed = false;
        
        try {
            for (int cartSlot = 0; cartSlot < fromItems.length; cartSlot++) {
                Item cartItem = fromItems[cartSlot];
                
                if (cartItem == null || cartItem.getAmount() == 0) {
                    continue;
                }
                
                try {
                    for (; invenIndex < inventories.length; invenIndex++) {
                        Item[] chestItems = inventories[invenIndex].getContents();
                        
                        for (int chestSlot = 0; chestSlot < chestItems.length; chestSlot++) {
                            Item chestItem = chestItems[chestSlot];
                            
                            if (chestItem == null) {
                                chestItems[chestSlot] = cartItem;
                                fromItems[cartSlot] = null;
                                setContents(inventories[invenIndex], chestItems);
                                changed = true;
                                throw new TransferredItemException();
                            } else if (chestItem.getItemId() == cartItem.getItemId()
                                    && chestItem.getAmount() < 64
                                    && chestItem.getAmount() >= 0) {
                                int spaceAvailable = 64 - chestItem.getAmount();
                                
                                if (spaceAvailable >= cartItem.getAmount()) {
                                    chestItem.setAmount(chestItem.getAmount()
                                            + cartItem.getAmount());
                                    fromItems[cartSlot] = null;
                                    setContents(inventories[invenIndex], chestItems);
                                    changed = true;
                                    throw new TransferredItemException();
                                } else {
                                    cartItem.setAmount(cartItem.getAmount()
                                            - spaceAvailable);
                                    chestItem.setAmount(64);
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
     * Move the contents of a chest block bag to an inventory.
     * 
     * @param to
     * @param bag
     */
    public static void moveChestBagToItemArray(ItemArray<?> to,
            NearbyChestBlockBag bag) {
        
        Item[] toItems = to.getContents();
        boolean changedDest = false;
        
        try {
            for (Inventory inventory : bag.getInventories()) {
                boolean changed = false;
                Item[] chestItems = inventory.getContents();
    
                try {
                    for (int chestSlot = 0; chestSlot < chestItems.length; chestSlot++) {
                        Item chestItem = chestItems[chestSlot];
                        
                        if (chestItem == null || chestItem.getAmount() == 0) {
                            continue;
                        }
                        
                        for (int cartSlot = 0; cartSlot < toItems.length; cartSlot++) {
                            Item cartItem = toItems[cartSlot];
        
                            if (cartItem == null) {
                                toItems[cartSlot] = chestItem;
                                chestItems[chestSlot] = null;
                                changed = true;
                                throw new TransferredItemException();
                            } else if (cartItem.getItemId() == chestItem.getItemId()
                                    && cartItem.getAmount() < 64
                                    && cartItem.getAmount() >= 0) {
                                int spaceAvailable = 64 - cartItem.getAmount();
                                
                                if (spaceAvailable >= chestItem.getAmount()) {
                                    cartItem.setAmount(cartItem.getAmount()
                                            + chestItem.getAmount());
                                    chestItems[chestSlot] = null;
                                    changed = true;
                                    throw new TransferredItemException();
                                } else {
                                    chestItem.setAmount(chestItem.getAmount()
                                            - spaceAvailable);
                                    cartItem.setAmount(64);
                                    changed = true;
                                }
                            }
                        }
                        
                        throw new TargetFullException();
                    }
                } catch (TransferredItemException e) {
                }
                
                if (changed) {
                    changedDest = true;
                    setContents(inventory, chestItems);
                }
            }
        } catch (TargetFullException e) {
        }
        
        if (changedDest) {
            setContents(to, toItems);
        }
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    public static void setContents(Inventory itemArray, Item[] contents) {
        int size = itemArray.getContentsSize();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i], i);
            }
        }
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    public static void setContents(ItemArray<?> itemArray, Item[] contents) {
        int size = itemArray.getContentsSize();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i], i);
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
