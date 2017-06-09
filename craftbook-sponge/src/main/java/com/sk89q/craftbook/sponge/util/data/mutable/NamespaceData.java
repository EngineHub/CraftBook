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

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableNamespaceData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class NamespaceData extends AbstractSingleData<String, NamespaceData, ImmutableNamespaceData> {

    public NamespaceData() {
        this("");
    }

    public NamespaceData(String value) {
        super(value, CraftBookKeys.NAMESPACE);
    }

    public Value<String> namespace() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.NAMESPACE, getValue());
    }

    @Override
    protected Value<String> getValueGetter() {
        return namespace();
    }

    @Override
    public Optional<NamespaceData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(NamespaceData.class).ifPresent((data) -> {
            NamespaceData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        });
        return Optional.of(this);
    }

    @Override
    public Optional<NamespaceData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.NAMESPACE.getQuery())) {
            return Optional.of(new NamespaceData(container.getString(CraftBookKeys.NAMESPACE.getQuery()).get()));
        }

        return Optional.empty();
    }

    @Override
    public NamespaceData copy() {
        return new NamespaceData(getValue());
    }

    @Override
    public ImmutableNamespaceData asImmutable() {
        return new ImmutableNamespaceData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(CraftBookKeys.NAMESPACE, getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
