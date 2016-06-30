package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;

public class ItemUtil {

    /**
     * Gets whether or not the specified {@link ItemStack} passes the {@link ItemStack}s.
     *
     * @param filters The filters
     * @param stack The stack to test
     * @return If it passes
     */
    public static boolean doesStackPassFilters(Collection<ItemStack> filters, ItemStack stack) {
        for(ItemStack filter : filters)
            if(stack == null && filter.getItem().getType() == ItemTypes.NONE || stack != null && filter.getItem().matches(stack))
                return true;
        return false;
    }
}
