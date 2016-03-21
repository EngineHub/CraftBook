/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
