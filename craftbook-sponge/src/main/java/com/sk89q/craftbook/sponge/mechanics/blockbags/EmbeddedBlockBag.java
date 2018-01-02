/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.blockbags;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class EmbeddedBlockBag implements DataSerializable, BlockBag {

    private List<ItemStack> itemStacks;

    public EmbeddedBlockBag() {
        super();
        itemStacks = new ArrayList<>();
    }

    @Override
    public boolean has(List<ItemStack> itemStacks) {
        List<ItemStack> testList = new ArrayList<>(itemStacks);
        Iterator<ItemStack> testIterator = testList.iterator();
        while (testIterator.hasNext()) {
            ItemStack testStack = testIterator.next();
            for (ItemStack itemStack : this.itemStacks) {
                if (itemStack.getType() == testStack.getType()) {
                    int newQuantity = testStack.getQuantity();
                    newQuantity -= itemStack.getQuantity();
                    if (newQuantity > 0) {
                        testStack.setQuantity(newQuantity);
                    } else {
                        testIterator.remove();
                    }
                }
            }
        }
        return testList.isEmpty();
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            itemAdd:
            {
                for (ItemStack existingStack : this.itemStacks) {
                    if (itemStack.getType() == existingStack.getType()) {
                        existingStack.setQuantity(existingStack.getQuantity() + itemStack.getQuantity());
                        break itemAdd;
                    }
                }

                this.itemStacks.add(itemStack);
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        List<ItemStack> unremovable = new ArrayList<>(itemStacks);
        Iterator<ItemStack> itemIterator = unremovable.iterator();
        while (itemIterator.hasNext()) {
            ItemStack itemStack = itemIterator.next();
            int newQuantity = itemStack.getQuantity();

            Iterator<ItemStack> existingIterator = this.itemStacks.iterator();
            while (existingIterator.hasNext()) {
                ItemStack existingStack = existingIterator.next();
                if (itemStack.getType() == existingStack.getType()) {
                    int numToRemove = Math.min(existingStack.getQuantity(), newQuantity);
                    newQuantity -=  numToRemove;
                    if (existingStack.getQuantity() - numToRemove <= 0) {
                        existingIterator.remove();
                    } else {
                        existingStack.setQuantity(existingStack.getQuantity() - numToRemove);
                    }
                }
            }

            if (newQuantity <= 0) {
                itemIterator.remove();
            } else {
                itemStack.setQuantity(newQuantity);
            }
        }

        return unremovable;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQuery.of("Items"), itemStacks);
    }

    public static class EmbeddedBlockBagBuilder extends AbstractDataBuilder<EmbeddedBlockBag> {

        public EmbeddedBlockBagBuilder() {
            super(EmbeddedBlockBag.class, 1);
        }

        @Override
        protected Optional<EmbeddedBlockBag> buildContent(DataView container) throws InvalidDataException {
            EmbeddedBlockBag embeddedBlockBag = new EmbeddedBlockBag();

            embeddedBlockBag.itemStacks = container.getSerializableList(DataQuery.of("Items"), ItemStack.class).orElse(Lists.newArrayList());

            return Optional.of(embeddedBlockBag);
        }
    }
}
