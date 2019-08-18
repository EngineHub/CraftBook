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
package org.enginehub.craftbook.sponge.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;

import java.util.Collection;
import java.util.Comparator;

public class ItemUtil {

    public static final Comparator<ItemStack> ALL_ANY_SIZE = Ordering.compound(ImmutableList.of(ItemStackComparators.TYPE, ItemStackComparators.PROPERTIES, ItemStackComparators.ITEM_DATA));

    /**
     * Gets whether or not the specified {@link ItemStack} passes the {@link ItemStack}s.
     *
     * @param filters The filters
     * @param stack The stack to test
     * @return If it passes
     */
    public static boolean doesStackPassFilters(Collection<ItemStack> filters, ItemStack stack) {
        for(ItemStack filter : filters)
            if(stack == null && filter.getType().getType() == ItemTypes.NONE || stack != null && filter.getType().matches(stack))
                return true;
        return false;
    }
}
