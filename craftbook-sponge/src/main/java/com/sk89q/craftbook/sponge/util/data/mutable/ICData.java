/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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

import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableICData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class ICData extends AbstractSingleData<SerializedICData, ICData, ImmutableICData> {

    public ICData() {
        this(null);
    }

    public ICData(SerializedICData value) {
        super(value, CraftBookKeys.IC_DATA);
    }

    public Value<SerializedICData> ic() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.IC_DATA, getValue());
    }

    @Override
    protected Value<SerializedICData> getValueGetter() {
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
            try {
                Class<SerializedICData> clazz = (Class<SerializedICData>) Class.forName(container.getString(DataQuery.of("ICDataClass")).orElse(SerializedICData.class.getName()));
                return Optional.of(new ICData(container.getSerializable(CraftBookKeys.IC_DATA.getQuery(), clazz).get()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
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
    public DataContainer toContainer() {
        return super.toContainer()
                .set(DataQuery.of("ICDataClass"), getValue().getClass().getName())
                .set(CraftBookKeys.IC_DATA, getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
