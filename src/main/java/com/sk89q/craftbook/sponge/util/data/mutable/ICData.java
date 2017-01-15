package com.sk89q.craftbook.sponge.util.data.mutable;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableICData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class ICData extends AbstractSingleData<IC, ICData, ImmutableICData> {

    public ICData() {
        this(null);
    }

    public ICData(IC value) {
        super(value, CraftBookKeys.IC_DATA);
    }

    public Value<IC> ic() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.IC_DATA, getValue());
    }

    @Override
    protected Value<IC> getValueGetter() {
        return ic();
    }

    @Override
    public Optional<ICData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(ICData.class).ifPresent((data) -> {
            ICData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        });
        return Optional.of(this);
    }

    @Override
    public Optional<ICData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.IC_DATA.getQuery())) {
            return Optional.of(new ICData(container.getSerializable(CraftBookKeys.IC_DATA.getQuery(), IC.class).get()));
        }

        return Optional.empty();
    }

    @Override
    public ICData copy() {
        return new ICData(getValue());
    }

    @Override
    public ImmutableICData asImmutable() {
        return new ImmutableICData(getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
