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

public class EmbeddedBlockBag extends BlockBag implements DataSerializable {

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
                if (itemStack.getItem() == testStack.getItem()) {
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
                    if (itemStack.getItem() == existingStack.getItem()) {
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
                if (itemStack.getItem() == existingStack.getItem()) {
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

        EmbeddedBlockBagBuilder() {
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
