package com.sk89q.craftbook.mech.crafting;

import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Silthus
 */
public class CraftingItemStack implements Comparable<CraftingItemStack> {

    private ItemStack item;

    //Advanced data
    private HashMap<String, Object> advancedData = new HashMap<String, Object>();

    public HashMap<String, Object> getAllAdvancedData() {

        return advancedData;
    }

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
        if(CraftBookPlugin.isDebugFlagEnabled("advanced-data"))
            CraftBookPlugin.logger().info("Adding advanced data of type: " + key + " to an ItemStack!");
        advancedData.put(key, data);
    }

    public CraftingItemStack(ItemStack item) {

        this.item = item;
        if(item != null && item.hasItemMeta()) { //We have some advanced data to set.

            if(item.getItemMeta().hasDisplayName())
                addAdvancedData("name", item.getItemMeta().getDisplayName());
            if(item.getItemMeta().hasLore())
                addAdvancedData("lore", item.getItemMeta().getLore());
        }
    }

    public ItemStack getItemStack() {

        return item;
    }

    public CraftingItemStack add(CraftingItemStack stack) {

        if (stack.isSameType(this)) {
            ItemUtil.addToStack(item, stack.getItemStack());
            advancedData.putAll(stack.getAllAdvancedData());
        }
        return this;
    }

    public boolean isSameType(CraftingItemStack stack) {

        return ItemUtil.areItemsIdentical(item, stack.item);
    }

    @Override
    public int compareTo(CraftingItemStack stack) {

        if (stack.getItemStack().getAmount() > item.getAmount()) return 1;
        if (stack.getItemStack().getAmount() == item.getAmount()) return 0;
        return -1;
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
            return isSameType(stack) && stack.getItemStack().getAmount() == getItemStack().getAmount();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {

        String me = getItemStack().getType().name();
        if(getItemStack().getDurability() > 0)
            me = me + ":" + getItemStack().getDurability();

        if(hasAdvancedData("name"))
            me = me + "|" + getAdvancedData("name");

        if(hasAdvancedData("lore")) {
            List<String> list = (List<String>)getAdvancedData("lore");
            for(String s : list)
                me = me + "|" + s;
        }

        return me;
    }
}
