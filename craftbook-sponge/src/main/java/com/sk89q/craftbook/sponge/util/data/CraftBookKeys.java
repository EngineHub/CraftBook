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
package com.sk89q.craftbook.sponge.util.data;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.reflect.TypeToken;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;

public class CraftBookKeys {

    public CraftBookKeys() {
    }

    public static Key<MutableBoundedValue<Integer>> LAST_POWER = Key.builder()
            .type(new TypeTokens.MutableBoundedValueIntegerTypeToken())
            .id("craftbook:lastpower")
            .name("LastPower")
            .query(of("LastPower"))
            .build();

    public static Key<Value<SerializedICData>> IC_DATA = Key.builder()
            .type(new TypeTokens.ICValueTypeToken())
            .id("craftbook:ic")
            .name("IC")
            .query(of("IC"))
            .build();

    public static Key<Value<String>> NAMESPACE = Key.builder()
            .type(new TypeToken<Value<String>>(){})
            .id("craftbook:namespace")
            .name("Namespace")
            .query(of("Namespace"))
            .build();

    public static Key<Value<Long>> BLOCK_BAG = Key.builder()
            .type(new TypeTokens.LongValueTypeToken())
            .id("craftbook:blockbag")
            .name("BlockBag")
            .query(of("BlockBag"))
            .build();

    public static Key<Value<EmbeddedBlockBag>> EMBEDDED_BLOCK_BAG = Key.builder()
            .type(new TypeToken<Value<EmbeddedBlockBag>>() {})
            .id("craftbook:embeddedblockbag")
            .name("EmbeddedBlockBag")
            .query(of("EmbeddedBlockBag"))
            .build();
}
