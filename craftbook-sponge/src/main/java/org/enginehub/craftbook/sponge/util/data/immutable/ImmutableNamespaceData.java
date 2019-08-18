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
import org.enginehub.craftbook.sponge.util.data.mutable.NamespaceData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableNamespaceData extends AbstractImmutableSingleData<String, ImmutableNamespaceData, NamespaceData> {

    public ImmutableNamespaceData() {
        this("");
    }

    public ImmutableNamespaceData(String value) {
        super(value, CraftBookKeys.NAMESPACE);
    }

    @Override
    protected ImmutableValue<String> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.NAMESPACE, getValue())
                .asImmutable();
    }

    @Override
    public NamespaceData asMutable() {
        return new NamespaceData(getValue());
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
