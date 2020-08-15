/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.cauldron;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Silthus
 */
public class CauldronItemStack implements Comparable<CauldronItemStack> {

    public static Collection<CauldronItemStack> convert(Collection<Item> stacks) {

        Set<ItemStack> items = new HashSet<>();
        for (Item item : stacks) {
            ItemStack stack = item.getItemStack();

            boolean has = false;
            Iterator<ItemStack> stackit = items.iterator();
            while (stackit.hasNext()) {
                ItemStack cstack = stackit.next();
                if (ItemUtil.areItemsIdentical(cstack, stack)) {
                    stackit.remove();
                    ItemUtil.addToStack(stack, cstack);
                    items.add(stack);
                    has = true;
                    break;
                }
            }
            if (!has)
                items.add(stack);
        }
        Set<CauldronItemStack> stackSet = new LinkedHashSet<>();
        // merge the amounts and stacks
        for (ItemStack stack : items)
            stackSet.add(new CauldronItemStack(stack));
        return stackSet;
    }

    private ItemStack item;

    public CauldronItemStack(ItemStack item) {

        this.item = item;
    }

    public ItemStack getItemStack() {

        return item;
    }

    public CauldronItemStack add(CauldronItemStack stack) {

        if (stack.isSameType(this)) {
            ItemUtil.addToStack(item, stack.item);
        }
        return this;
    }

    public boolean isSameType(CauldronItemStack stack) {

        return ItemUtil.areItemsIdentical(item, stack.item);
    }

    @Override
    public int compareTo(CauldronItemStack stack) {
        return Integer.compare(stack.item.getAmount(), item.getAmount());
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + item.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof CauldronItemStack) {
            CauldronItemStack stack = (CauldronItemStack) obj;
            return isSameType(stack) && stack.item.getAmount() == item.getAmount();
        }
        return false;
    }

    @Override
    public String toString() {

        return ItemSyntax.getStringFromItem(item);
    }
}