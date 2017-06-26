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

import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableEmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class EmbeddedBlockBagData extends AbstractSingleData<EmbeddedBlockBag, EmbeddedBlockBagData, ImmutableEmbeddedBlockBagData> {

    public EmbeddedBlockBagData() {
        this(null);
    }

    public EmbeddedBlockBagData(EmbeddedBlockBag value) {
        super(value, CraftBookKeys.EMBEDDED_BLOCK_BAG);
    }

    public Value<EmbeddedBlockBag> embeddedBlockBag() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.EMBEDDED_BLOCK_BAG, getValue());
    }

    @Override
    protected Value<EmbeddedBlockBag> getValueGetter() {
        return embeddedBlockBag();
    }

    @Override
    public Optional<EmbeddedBlockBagData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(EmbeddedBlockBagData.class).ifPresent((data) -> {
            EmbeddedBlockBagData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        });
        return Optional.of(this);
    }

    @Override
    public Optional<EmbeddedBlockBagData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.EMBEDDED_BLOCK_BAG.getQuery())) {
            return Optional.of(new EmbeddedBlockBagData(container.getSerializable(CraftBookKeys.EMBEDDED_BLOCK_BAG.getQuery(),
                    EmbeddedBlockBag.class).get()));
        }

        return Optional.empty();
    }

    @Override
    public EmbeddedBlockBagData copy() {
        return new EmbeddedBlockBagData(getValue());
    }

    @Override
    public ImmutableEmbeddedBlockBagData asImmutable() {
        return new ImmutableEmbeddedBlockBagData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CraftBookKeys.EMBEDDED_BLOCK_BAG, getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
