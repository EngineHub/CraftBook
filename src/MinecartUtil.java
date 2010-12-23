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

import com.sk89q.craftbook.BlockSourceException;

/**
 * Minecart related functions.
 * 
 * @author sk89q
 */
public class MinecartUtil {
	/**
	 * Move the contents of a storage minecart's chest to a chest block bag.
	 * 
	 * @param minecart
	 * @param bag
	 */
	public static void storageToChestBag(Minecart minecart,
			NearbyChestBlockBag bag) {
		
	    StorageMinecart sm = minecart.getStorage();
	    Item[] items = sm.getContents();
	    
	    try {
	        // Loop through each filled position in the storage cart
	        for (int i = 0; items.length > i; i++) {
	            if (items[i] == null) {
	                continue;
	            }
	            
	            // Move the items into the chest one at a time.
	            while (items[i].getAmount() > 0) {
	            	bag.storeBlock(items[i].getItemId());
	                items[i].setAmount(items[i].getAmount() - 1);
	            }
	            
	            // Now that all of that item is gone, null the position
	            items[i] = null;
	        }
	    } catch (BlockSourceException e) {
	    	// No room left!
		}
	    
	    sm.setContents(items);
	}

	/**
	 * Move the contents of a chest block bag to a storage minecart's chest.
	 * 
	 * @param minecart
	 * @param bag
	 */
	public static void chestBagToStorage(Minecart minecart,
			NearbyChestBlockBag bag) {
		
	    StorageMinecart sm = minecart.getStorage();
	    Item[] cartItems = sm.getContents();
	    boolean changedCart = false;
		
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
						
						for (int cartSlot = 0; cartSlot < cartItems.length; cartSlot++) {
							Item cartItem = cartItems[cartSlot];
		
							if (cartItem == null) {
								cartItems[cartSlot] = chestItem;
								chestItems[chestSlot] = null;
								changed = true;
								throw new TransferredItemException();
							} else if (cartItem.getItemId() == chestItem.getItemId()
									&& cartItem.getAmount() < 64) {
								int leftOver = cartItem.getAmount();
								
								if (leftOver >= chestItem.getAmount()) {
									cartItem.setAmount(cartItem.getAmount() + leftOver);
									chestItems[chestSlot] = null;
									changed = true;
									throw new TransferredItemException();
								} else {
									chestItem.setAmount(chestItem.getAmount()
											- (64 - cartItem.getAmount()));
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
					changedCart = true;
					inventory.setContents(chestItems);
				}
			}
	    } catch (TargetFullException e) {
	    }
	    
	    if (changedCart) {
	    	minecart.getStorage().setContents(cartItems);
	    }
	}

	/**
	 * Thrown when an item has been fully transferred.
	 */
	private static class TransferredItemException extends Exception {
	}

	/**
	 * Thrown when the target is full.
	 */
	private static class TargetFullException extends Exception {
	}
}
