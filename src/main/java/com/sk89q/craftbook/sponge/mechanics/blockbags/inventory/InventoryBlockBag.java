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
package com.sk89q.craftbook.sponge.mechanics.blockbags.inventory;

import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBag;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class InventoryBlockBag extends BlockBag {

    private Inventory inventory;

    public InventoryBlockBag(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean has(List<ItemStack> itemStacks) {
        for(ItemStack stack : itemStacks) {
            if(!inventory.contains(stack))
                return false;
        }
        return true;
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        List<ItemStack> output = new ArrayList<>();
        for(ItemStack stack : itemStacks) {
            InventoryTransactionResult result = inventory.offer(stack);
            if(result.getRejectedItems().size() > 0) {
                output.addAll(result.getRejectedItems().stream().map(ItemStackSnapshot::createStack).collect(Collectors.toList()));
            }
        }
        return output;
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        List<ItemStack> output = new ArrayList<>();
        for(ItemStack stack : itemStacks) {
            Inventory view = inventory.query(stack);
            //view.poll(stack.getQuantity());
            //InventoryTransactionResult result = ;
            //if(result.getRejectedItems().size() > 0) {
            //    output.addAll(result.getRejectedItems().stream().map(ItemStackSnapshot::createStack).collect(Collectors.toList()));
            //}
        }
        //TODO
        return itemStacks;
    }
}
