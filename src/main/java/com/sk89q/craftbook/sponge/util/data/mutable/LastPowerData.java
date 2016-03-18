package com.sk89q.craftbook.sponge.util.data.mutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableLastPowerData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractBoundedComparableData;
import org.spongepowered.api.data.merge.MergeFunction;

import java.util.Optional;

public class LastPowerData extends AbstractBoundedComparableData<Integer, LastPowerData, ImmutableLastPowerData> {

    public LastPowerData() {
        this(0);
    }

    public LastPowerData(int lastPower) {
        super(lastPower, CraftBookKeys.LAST_POWER, Integer::compareTo, 0, 15, 0);
    }

    @Override
    public Optional<LastPowerData> fill(DataHolder dataHolder, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<LastPowerData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.LAST_POWER.getQuery())) {
            return Optional.of(new LastPowerData(container.getInt(CraftBookKeys.LAST_POWER.getQuery()).get()));
        }

        return Optional.empty();
    }

    @Override
    public LastPowerData copy() {
        return new LastPowerData(getValueGetter().get());
    }

    @Override
    public ImmutableLastPowerData asImmutable() {
        return new ImmutableLastPowerData(getValueGetter().get());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
