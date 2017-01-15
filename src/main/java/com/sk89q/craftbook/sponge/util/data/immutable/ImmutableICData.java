package com.sk89q.craftbook.sponge.util.data.immutable;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.ICData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class ImmutableICData extends AbstractImmutableSingleData<IC, ImmutableICData, ICData> {

    public ImmutableICData(IC value) {
        super(value, CraftBookKeys.IC_DATA);
    }

    @Override
    protected ImmutableValue<IC> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.IC_DATA, getValue())
                .asImmutable();
    }

    @Override
    public ICData asMutable() {
        return new ICData(getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
