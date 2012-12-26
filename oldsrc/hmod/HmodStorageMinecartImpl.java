/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.craftbook.access.StorageMinecartInterface;
import com.sk89q.craftbook.access.WorldInterface;

//Java's lack of multipul implementation inhertance is annoying!
public class HmodStorageMinecartImpl extends HmodMinecartImpl
        implements StorageMinecartInterface {

    private final ItemArray<?> inventory;
    private final int length;
    private final com.sk89q.craftbook.access.Item[] items;
    private final boolean[] changed;

    public HmodStorageMinecartImpl(Minecart cart, WorldInterface w) {

        super(cart, w);
        inventory = cart.getStorage();
        length = inventory.getContentsSize();
        items = new com.sk89q.craftbook.access.Item[length];
        changed = new boolean[length];
        Item[] items = inventory.getContents();
        for (int j = 0; j < length; j++) {
            Item item = items[j];
            com.sk89q.craftbook.access.Item cbItem;
            if (item != null) cbItem = new com.sk89q.craftbook.access.Item(item.getItemId(),
                    item.getAmount());
            else cbItem = new com.sk89q.craftbook.access.Item(0, 0);

            this.items[j] = cbItem;
        }
    }

    public int getLength() {

        return length;
    }

    public com.sk89q.craftbook.access.Item getItem(int slot) {

        return items[slot];
    }

    public void setItem(int slot, com.sk89q.craftbook.access.Item item) {

        items[slot] = item;
        changed[slot] = true;
    }

    public com.sk89q.craftbook.access.Item[] getItems() {

        return items.clone();
    }

    public void flushChanges() {

        for (int i = 0; i < length; i++) {
            if (changed[i]) {
                com.sk89q.craftbook.access.Item item = items[i];
                if (item.id == 0 || item.count == 0) continue;
                inventory.setSlot(new Item(item.id, item.count), i);
                changed[i] = false;
            }
        }
    }
}

