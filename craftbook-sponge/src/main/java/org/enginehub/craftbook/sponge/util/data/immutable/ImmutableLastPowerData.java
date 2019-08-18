/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package org.enginehub.craftbook.sponge.util.data.immutable;

import org.enginehub.craftbook.sponge.util.data.CraftBookKeys;
import org.enginehub.craftbook.sponge.util.data.mutable.LastPowerData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableBoundedComparableData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
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