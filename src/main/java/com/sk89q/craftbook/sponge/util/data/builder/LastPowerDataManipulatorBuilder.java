package com.sk89q.craftbook.sponge.util.data.builder;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableLastPowerData;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class LastPowerDataManipulatorBuilder implements DataManipulatorBuilder<LastPowerData, ImmutableLastPowerData> {

    @Override
    public LastPowerData create() {
        return new LastPowerData();
    }

    @Override
    public Optional<LastPowerData> createFrom(DataHolder dataHolder) {
        return Optional.of(dataHolder.get(LastPowerData.class).orElse(new LastPowerData()));
    }

    @Override
    public Optional<LastPowerData> build(DataView container) throws InvalidDataException {
        if (container.contains(CraftBookKeys.LAST_POWER)) {
            final int lastPower = container.getInt(CraftBookKeys.LAST_POWER.getQuery()).get();
            return Optional.of(new LastPowerData(lastPower));
        }
        return Optional.empty();
    }
}
