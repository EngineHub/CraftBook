/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.blockbags;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;

/**
 * An interface for accessing multiple block bags at the same time.
 */
public class MultiBlockBag extends BlockBag {

    BlockBag[] bags;

    public MultiBlockBag(BlockBag... bags) {
        this.bags = bags;
    }

    @Override
    public boolean has(List<ItemStack> itemStacks) {
        for (BlockBag bag : bags) {
            if (itemStacks.size() == 0) break;
            if (bag.has(itemStacks))
                return true;
        }

        return false;
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        for (BlockBag bag : bags) {
            if (itemStacks.size() == 0) break;
            itemStacks = bag.add(itemStacks);
        }

        return itemStacks;
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        for (BlockBag bag : bags) {
            if (itemStacks.size() == 0) break;
            itemStacks = bag.remove(itemStacks);
        }

        return itemStacks;
    }

}
