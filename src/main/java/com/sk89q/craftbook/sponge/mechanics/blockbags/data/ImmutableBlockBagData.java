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
package com.sk89q.craftbook.sponge.mechanics.blockbags.data;

import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class ImmutableBlockBagData extends AbstractImmutableSingleData<Long, ImmutableBlockBagData, BlockBagData> {

    public ImmutableBlockBagData(Long value) {
        super(value, BlockBagManager.BLOCK_BAG);
    }

    @Override
    protected ImmutableValue<Long> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(BlockBagManager.BLOCK_BAG, getValue())
                .asImmutable();
    }

    @Override
    public BlockBagData asMutable() {
        return new BlockBagData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(BlockBagManager.BLOCK_BAG, this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
