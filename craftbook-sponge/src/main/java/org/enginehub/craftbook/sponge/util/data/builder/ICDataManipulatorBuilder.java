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
package org.enginehub.craftbook.sponge.util.data.builder;

import org.enginehub.craftbook.sponge.mechanics.ics.SerializedICData;
import org.enginehub.craftbook.sponge.util.data.CraftBookKeys;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableICData;
import org.enginehub.craftbook.sponge.util.data.mutable.ICData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
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
            try {
                Class<SerializedICData> clazz = (Class<SerializedICData>) Class.forName(container.getString(DataQuery.of("ICDataClass")).orElse(SerializedICData.class.getName()));
                return Optional.of(new ICData(container.getSerializable(CraftBookKeys.IC_DATA.getQuery(), clazz).get()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }
}
