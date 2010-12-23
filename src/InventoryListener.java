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
 * Listener for inventory things.
 * 
 * @author sk89q
 */
public class InventoryListener extends CraftBookDelegateListener {
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public InventoryListener(CraftBook craftBook, CraftBookListener listener) {
		super(craftBook, listener);
	}

    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
    }

    /**
     * Called when a player's inventory is modified.
     * 
     * @param player
     *            player who's inventory was modified
     * @return true if you want any changes to be reverted
     */
    public boolean onInventoryChange(Player player) {
    	//Inventory inventory = player.getInventory();
    	//Item[] items = inventory.getContents();

    	/*player.sendMessage("got item 0: " + (items[0] != null ? items[0].getItemId() : "EMPTY"));
    	player.sendMessage("got item 1: " + (items[1] != null ? items[1].getItemId() : "EMPTY"));
    	player.sendMessage("got item 2: " + (items[2] != null ? items[2].getItemId() : "EMPTY"));
    	player.sendMessage("got item 3: " + (items[3] != null ? items[3].getItemId() : "EMPTY"));
    	player.sendMessage("got item 4: " + (items[4] != null ? items[4].getItemId() : "EMPTY"));*/
    	
        return false;
    }
}
