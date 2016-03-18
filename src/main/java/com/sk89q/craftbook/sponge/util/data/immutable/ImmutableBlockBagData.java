package com.sk89q.craftbook.sponge.util.data.immutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.BlockBagData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableBlockBagData extends AbstractImmutableSingleData<Long, ImmutableBlockBagData, BlockBagData> {

    public ImmutableBlockBagData(Long value) {
        super(value, CraftBookKeys.BLOCK_BAG);
    }

    @Override
    protected ImmutableValue<Long> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.BLOCK_BAG, getValue())
                .asImmutable();
    }

    @Override
    public BlockBagData asMutable() {
        return new BlockBagData(getValue());
    }

    @Override
    public int compareTo(ImmutableBlockBagData o) {
        return Long.compare(getValue(), o.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
