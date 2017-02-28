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
import java.util.List;
import java.util.Optional;

public class EmbeddedBlockBag extends BlockBag implements DataSerializable {

    private List<ItemStack> itemStacks;

    public EmbeddedBlockBag() {
        itemStacks = new ArrayList<>();
    }

    @Override
    public boolean has(List<ItemStack> itemStacks) {
        return false;
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        return null;
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        return null;
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
