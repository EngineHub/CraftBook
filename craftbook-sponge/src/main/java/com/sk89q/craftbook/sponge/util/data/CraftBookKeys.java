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
package com.sk89q.craftbook.sponge.util.data;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

import com.google.common.reflect.TypeToken;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;

public class CraftBookKeys {

    public static Key<MutableBoundedValue<Integer>> LAST_POWER = makeSingleKey(new TypeTokens.IntegerTypeToken(),
            new TypeTokens.MutableBoundedValueIntegerTypeToken(), of("LastPower"), "craftbook:lastpower", "LastPower");

    public static Key<Value<SerializedICData>> IC_DATA = makeSingleKey(new TypeTokens.ICTypeToken(),
            new TypeTokens.ICValueTypeToken(), of("IC"), "craftbook:ic", "IC");

    public static Key<Value<String>> NAMESPACE = makeSingleKey(TypeToken.of(String.class),
            new TypeToken<Value<String>>(){}, of("Namespace"), "craftbook:namespace", "Namespace");

    public static Key<Value<Long>> BLOCK_BAG = makeSingleKey(new TypeTokens.LongTypeToken(),
            new TypeTokens.LongValueTypeToken(), of("BlockBag"), "craftbook:blockbag", "BlockBag");

    public static Key<Value<EmbeddedBlockBag>> EMBEDDED_BLOCK_BAG = makeSingleKey(new TypeToken<EmbeddedBlockBag>() {},
            new TypeToken<Value<EmbeddedBlockBag>>() {}, of("EmbeddedBlockBag"), "craftbook:embeddedblockbag",
            "EmbeddedBlockBag");
}
