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
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableBlockBagData;
import com.sk89q.craftbook.sponge.util.data.util.AbstractLongData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class BlockBagData extends AbstractLongData<BlockBagData, ImmutableBlockBagData> {

    public BlockBagData() {
        this(0);
    }

    public BlockBagData(long value) {
        super(value, CraftBookKeys.BLOCK_BAG);
    }

    public Value<Long> blockBag() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.BLOCK_BAG, getValue());
    }

    @Override
    protected Value<Long> getValueGetter() {
        return blockBag();
    }

    @Override
    public Optional<BlockBagData> fill(DataHolder dataHolder, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<BlockBagData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.BLOCK_BAG.getQuery())) {
            return Optional.of(new BlockBagData(container.getLong(CraftBookKeys.BLOCK_BAG.getQuery()).get()));
        }

        return Optional.empty();
    }

    @Override
    public BlockBagData copy() {
        return new BlockBagData(getValueGetter().get());
    }

    @Override
    public ImmutableBlockBagData asImmutable() {
        return new ImmutableBlockBagData(getValueGetter().get());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
