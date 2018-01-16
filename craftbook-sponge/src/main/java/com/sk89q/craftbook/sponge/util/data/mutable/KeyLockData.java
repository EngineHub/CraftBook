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
package com.sk89q.craftbook.sponge.util.data.mutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableKeyLockData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class KeyLockData extends AbstractSingleData<ItemStackSnapshot, KeyLockData, ImmutableKeyLockData> {

    public KeyLockData() {
        this(ItemStackSnapshot.NONE);
    }

    public KeyLockData(ItemStackSnapshot value) {
        super(value, CraftBookKeys.KEY_LOCK);
    }

    public Value<ItemStackSnapshot> keyLock() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.KEY_LOCK, getValue());
    }

    @Override
    protected Value<ItemStackSnapshot> getValueGetter() {
        return keyLock();
    }

    @Override
    public Optional<KeyLockData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(KeyLockData.class).ifPresent((data) -> {
            KeyLockData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        });
        return Optional.of(this);
    }

    @Override
    public Optional<KeyLockData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.KEY_LOCK.getQuery())) {
            return Optional.of(new KeyLockData(container.getSerializable(CraftBookKeys.KEY_LOCK.getQuery(), ItemStackSnapshot.class).get()));
        }

        return Optional.empty();
    }

    @Override
    public KeyLockData copy() {
        return new KeyLockData(getValue());
    }

    @Override
    public ImmutableKeyLockData asImmutable() {
        return new ImmutableKeyLockData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CraftBookKeys.KEY_LOCK, this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
