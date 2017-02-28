package com.sk89q.craftbook.sponge.mechanics.blockbags.data;

import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class EmbeddedBlockBagData extends AbstractSingleData<EmbeddedBlockBag, EmbeddedBlockBagData, ImmutableEmbeddedBlockBagData> {

    public EmbeddedBlockBagData() {
        this(null);
    }

    public EmbeddedBlockBagData(EmbeddedBlockBag value) {
        super(value, BlockBagManager.EMBEDDED_BLOCK_BAG);
    }

    public Value<EmbeddedBlockBag> embeddedBlockBag() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(BlockBagManager.EMBEDDED_BLOCK_BAG, getValue());
    }

    @Override
    protected Value<EmbeddedBlockBag> getValueGetter() {
        return embeddedBlockBag();
    }

    @Override
    public Optional<EmbeddedBlockBagData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(EmbeddedBlockBagData.class).ifPresent((data) -> {
            EmbeddedBlockBagData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        });
        return Optional.of(this);
    }

    @Override
    public Optional<EmbeddedBlockBagData> from(DataContainer container) {
        if (container.contains(BlockBagManager.EMBEDDED_BLOCK_BAG.getQuery())) {
            return Optional.of(new EmbeddedBlockBagData(container.getSerializable(BlockBagManager.EMBEDDED_BLOCK_BAG.getQuery(),
                    EmbeddedBlockBag.class).get()));
        }

        return Optional.empty();
    }

    @Override
    public EmbeddedBlockBagData copy() {
        return new EmbeddedBlockBagData(getValue());
    }

    @Override
    public ImmutableEmbeddedBlockBagData asImmutable() {
        return new ImmutableEmbeddedBlockBagData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(BlockBagManager.EMBEDDED_BLOCK_BAG, getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
