package com.sk89q.craftbook.sponge.util.data.mutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableBlockBagData;
import com.sk89q.craftbook.sponge.util.data.util.AbstractLongData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class BlockBagData extends AbstractLongData<BlockBagData, ImmutableBlockBagData> {

    public BlockBagData() {
        this(0);
    }

    public BlockBagData(long value) {
        super(value, CraftBookKeys.BLOCK_BAG);
    }

    public Value<Long> blockBag() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.BLOCK_BAG, getValue());
    }

    @Override
    protected Value<Long> getValueGetter() {
        return blockBag();
    }

    @Override
    public Optional<BlockBagData> fill(DataHolder dataHolder, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<BlockBagData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.BLOCK_BAG.getQuery())) {
            return Optional.of(new BlockBagData(container.getLong(CraftBookKeys.BLOCK_BAG.getQuery()).get()));
        }

        return Optional.empty();
    }

    @Override
    public BlockBagData copy() {
        return new BlockBagData(getValueGetter().get());
    }

    @Override
    public ImmutableBlockBagData asImmutable() {
        return new ImmutableBlockBagData(getValueGetter().get());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
