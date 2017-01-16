package com.sk89q.craftbook.sponge.util.data.builder;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableICData;
import com.sk89q.craftbook.sponge.util.data.mutable.ICData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class ICDataManipulatorBuilder extends AbstractDataBuilder<ICData> implements DataManipulatorBuilder<ICData, ImmutableICData> {

    public ICDataManipulatorBuilder() {
        super(ICData.class, 1);
    }

    @Override
    public ICData create() {
        return new ICData();
    }

    @Override
    public Optional<ICData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<ICData> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(CraftBookKeys.IC_DATA.getQuery())) {
            IC ic = container.getSerializable(CraftBookKeys.IC_DATA.getQuery(), IC.class).get();
            return Optional.of(new ICData(ic));
        }

        return Optional.empty();
    }
}
