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

import org.enginehub.craftbook.sponge.mechanics.ics.SerializedICData;
import org.enginehub.craftbook.sponge.util.data.CraftBookKeys;
import org.enginehub.craftbook.sponge.util.data.mutable.ICData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class ImmutableICData extends AbstractImmutableSingleData<SerializedICData, ImmutableICData, ICData> {

    public ImmutableICData() {
        this(null);
    }

    public ImmutableICData(SerializedICData value) {
        super(value, CraftBookKeys.IC_DATA);
    }

    @Override
    protected ImmutableValue<SerializedICData> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.IC_DATA, getValue())
                .asImmutable();
    }

    @Override
    public ICData asMutable() {
        return new ICData(getValue());
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
