package com.sk89q.craftbook.mechanics.cauldron;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Silthus
 */
public class CauldronItemStack implements Comparable<CauldronItemStack> {

    public static Collection<CauldronItemStack> convert(Collection<Item> stacks) {

        Set<ItemStack> items = new HashSet<ItemStack>();
        for (Item item : stacks) {
            ItemStack stack = item.getItemStack();

            boolean has = false;
            Iterator<ItemStack> stackit = items.iterator();
            while(stackit.hasNext()) {
                ItemStack cstack = stackit.next();
                if(ItemUtil.areItemsIdentical(cstack, stack)) {
                    stackit.remove();
                    ItemUtil.addToStack(stack, cstack);
                    items.add(stack);
                    has = true;
                    break;
                }
            }
            if(!has)
                items.add(stack);
        }
        Set<CauldronItemStack> stackSet = new LinkedHashSet<CauldronItemStack>();
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
            ItemUtil.addToStack(item, stack.getItemStack());
        }
        return this;
    }

    public boolean isSameType(CauldronItemStack stack) {

        return ItemUtil.areItemsIdentical(item, stack.item);
    }

    @Override
    public int compareTo(CauldronItemStack stack) {

        if (stack.getItemStack().getAmount() > item.getAmount()) return 1;
        if (stack.getItemStack().getAmount() == item.getAmount()) return 0;
        return -1;
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
            return isSameType(stack) && stack.getItemStack().getAmount() == getItemStack().getAmount();
        }
        return false;
    }

    @Override
    public String toString() {

        return ItemSyntax.getStringFromItem(getItemStack());
    }
}