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

package com.sk89q.craftbook.mechanics.crafting;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Silthus
 */
public class CraftingItemStack implements Comparable<CraftingItemStack> {

    private ItemStack item;

    //Advanced data
    private HashMap<String, Object> advancedData = new HashMap<>();

    public boolean hasAdvancedData() {
        return !advancedData.isEmpty();
    }

    public boolean hasAdvancedData(String key) {
        return advancedData.containsKey(key);
    }

    public Object getAdvancedData(String key) {
        return advancedData.get(key);
    }

    public void addAdvancedData(String key, Object data) {
        CraftBookPlugin.logDebugMessage("Adding advanced data of type: " + key + " to an ItemStack!", "advanced-data.init");
        advancedData.put(key, data);
    }

    public CraftingItemStack(ItemStack item) {
        this.item = item;
        if(item != null && item.hasItemMeta()) //We have some advanced data to set.
            addAdvancedData("item-meta", true);
    }

    public ItemStack getItemStack() {
        return item;
    }

    public CraftingItemStack add(CraftingItemStack stack) {
        if (stack.isSameType(this)) {
            ItemUtil.addToStack(item, stack.item);
            advancedData.putAll(stack.advancedData);
        }
        return this;
    }

    boolean isSameType(CraftingItemStack stack) {
        return ItemUtil.areItemsIdentical(item, stack.item);
    }

    @Override
    public int compareTo(CraftingItemStack stack) {
        return Integer.compare(stack.item.getAmount(), item.getAmount());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + item.hashCode();
        result = prime * result + advancedData.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftingItemStack) {
            CraftingItemStack stack = (CraftingItemStack) obj;
            if(stack.advancedData.size() != advancedData.size()) {
                return false;
            }
            for(Map.Entry<String, Object> advancedDataEntries : advancedData.entrySet()) {
                if (!stack.hasAdvancedData(advancedDataEntries.getKey())) {
                    return false;
                } else if (!advancedDataEntries.getValue().equals(stack.getAdvancedData(advancedDataEntries.getKey()))){
                    return false;
                }
            }
            return isSameType(stack) && stack.item.getAmount() == item.getAmount();
        }
        return false;
    }

    @Override
    public String toString() {
        String it = ItemSyntax.getStringFromItem(item);
        if(hasAdvancedData("chance"))
            it = it + '%' + getAdvancedData("chance");
        return it;
    }
}