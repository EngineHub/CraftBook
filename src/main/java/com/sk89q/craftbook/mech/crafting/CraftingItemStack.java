package com.sk89q.craftbook.mech.crafting;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.util.RegexUtil;

/**
 * @author Silthus
 */
public class CraftingItemStack implements Comparable<CraftingItemStack> {

    public static Collection<CraftingItemStack> convert(Collection<Item> stacks) {

        Map<String, Integer> items = new HashMap<String, Integer>();
        for (Item item : stacks) {
            ItemStack stack = item.getItemStack();
            String name = stack.getType() + ":" + stack.getDurability();
            if (items.containsKey(name)) {
                items.put(name, items.get(name) + stack.getAmount());
            } else {
                items.put(name, stack.getAmount());
            }
        }
        Set<CraftingItemStack> stackSet = new LinkedHashSet<CraftingItemStack>();
        // merge the amounts and stacks
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String[] split = RegexUtil.COLON_PATTERN.split(entry.getKey());
            stackSet.add(new CraftingItemStack(Material.getMaterial(split[0]), Short.parseShort(split[1]),
                    entry.getValue()));
        }
        return stackSet;
    }

    private Material material;
    private short data;
    private int amount;

    public CraftingItemStack(Material material, short data, int amount) {

        this.material = material;
        this.data = data;
        this.amount = amount;
    }

    public CraftingItemStack(Material material, int amount) {

        this.material = material;
        this.amount = amount;
        data = 0;
    }

    public CraftingItemStack(Material material, short data) {

        this(material, data, 0);
    }

    public CraftingItemStack(Material material) {

        this(material, 0);
    }

    public Material getMaterial() {

        return material;
    }

    public short getData() {

        return data;
    }

    public int getAmount() {

        if (amount < 1) return 1;
        return amount;
    }

    public void setMaterial(Material material) {

        this.material = material;
    }

    public void setData(short data) {

        this.data = data;
    }

    public void setAmount(int amount) {

        this.amount = amount;
    }

    public ItemStack getItemStack() {

        return new ItemStack(material, amount, data);
    }

    public CraftingItemStack add(CraftingItemStack stack) {

        if (stack.equals(this)) {
            amount += stack.getAmount();
        }
        return this;
    }

    public boolean isSameType(CraftingItemStack stack) {

        if (data == -1 || stack.getData() == -1) return stack.getMaterial() == getMaterial();
        return stack.getMaterial() == getMaterial() && stack.getData() == getData();
    }

    @Override
    public int compareTo(CraftingItemStack stack) {

        if (getAmount() > stack.getAmount()) return 1;
        if (getAmount() == stack.getAmount()) return 0;
        return -1;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + amount;
        result = prime * result + data;
        result = prime * result + (material == null ? 0 : material.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof CraftingItemStack) {
            CraftingItemStack stack = (CraftingItemStack) obj;
            return isSameType(stack) && stack.getAmount() == getAmount();
        }
        return false;
    }
}
