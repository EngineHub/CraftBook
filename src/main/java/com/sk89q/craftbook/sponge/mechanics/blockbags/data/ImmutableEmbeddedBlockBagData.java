package com.sk89q.craftbook.sponge.mechanics.blockbags.data;

import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableEmbeddedBlockBagData extends AbstractImmutableSingleData<EmbeddedBlockBag, ImmutableEmbeddedBlockBagData, EmbeddedBlockBagData> {

    protected ImmutableEmbeddedBlockBagData() {
        this(null);
    }

    protected ImmutableEmbeddedBlockBagData(EmbeddedBlockBag value) {
        super(value, BlockBagManager.EMBEDDED_BLOCK_BAG);
    }

    @Override
    protected ImmutableValue<EmbeddedBlockBag> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(BlockBagManager.EMBEDDED_BLOCK_BAG, getValue())
                .asImmutable();
    }

    @Override
    public EmbeddedBlockBagData asMutable() {
        return new EmbeddedBlockBagData(getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
