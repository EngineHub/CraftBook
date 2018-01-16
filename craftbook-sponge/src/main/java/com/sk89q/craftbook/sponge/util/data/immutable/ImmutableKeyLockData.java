/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.util.data.immutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.KeyLockData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class ImmutableKeyLockData extends AbstractImmutableSingleData<ItemStackSnapshot, ImmutableKeyLockData, KeyLockData> {

    public ImmutableKeyLockData() {
        this(ItemStackSnapshot.NONE);
    }

    public ImmutableKeyLockData(ItemStackSnapshot value) {
        super(value, CraftBookKeys.KEY_LOCK);
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.KEY_LOCK, getValue())
                .asImmutable();
    }

    @Override
    public KeyLockData asMutable() {
        return new KeyLockData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CraftBookKeys.KEY_LOCK, getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
