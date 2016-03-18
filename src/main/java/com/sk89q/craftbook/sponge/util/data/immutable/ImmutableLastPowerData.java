package com.sk89q.craftbook.sponge.util.data.immutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableBoundedComparableData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;

public class ImmutableLastPowerData extends AbstractImmutableBoundedComparableData<Integer, ImmutableLastPowerData, LastPowerData> {
    public ImmutableLastPowerData() {
        this(0);
    }

    public ImmutableLastPowerData(int value) {
        this(value, 0, 15);
    }

    public ImmutableLastPowerData(int value, int lowerBound, int upperBound) {
        this(value, lowerBound, upperBound, 0);
    }

    public ImmutableLastPowerData(int value, int lowerBound, int upperBound, int defaultValue) {
        super(value, CraftBookKeys.LAST_POWER, Integer::compareTo, lowerBound, upperBound, defaultValue);
    }

    public ImmutableBoundedValue<Integer> lastPower() {
        return this.getValueGetter();
    }

    @Override
    public LastPowerData asMutable() {
        return new LastPowerData(this.value);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CraftBookKeys.LAST_POWER, this.value);
    }
}