package com.sk89q.craftbook.sponge.util.data.util;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.value.BaseValue;

public abstract class AbstractLongData<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends AbstractSingleData<Long, M, I> {

    protected AbstractLongData(long value, Key<? extends BaseValue<Long>> usedKey) {
        super(value, usedKey);
    }

    @Override
    public int compareTo(M o) {
        return Long.compare(o.get(this.usedKey).get(), this.getValue());
    }
}
