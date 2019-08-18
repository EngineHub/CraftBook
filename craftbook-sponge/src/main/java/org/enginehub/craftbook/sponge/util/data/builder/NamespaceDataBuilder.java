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

import org.enginehub.craftbook.sponge.util.data.CraftBookKeys;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableNamespaceData;
import org.enginehub.craftbook.sponge.util.data.mutable.NamespaceData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class NamespaceDataBuilder extends AbstractDataBuilder<NamespaceData> implements DataManipulatorBuilder<NamespaceData, ImmutableNamespaceData> {

    public NamespaceDataBuilder() {
        super(NamespaceData.class, 1);
    }

    @Override
    public NamespaceData create() {
        return new NamespaceData();
    }

    @Override
    public Optional<NamespaceData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<NamespaceData> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(CraftBookKeys.NAMESPACE.getQuery())) {
            return Optional.of(new NamespaceData(container.getString(CraftBookKeys.NAMESPACE.getQuery()).get()));
        }

        return Optional.empty();
    }
}
