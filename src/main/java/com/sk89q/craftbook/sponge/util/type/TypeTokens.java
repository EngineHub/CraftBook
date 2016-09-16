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
package com.sk89q.craftbook.sponge.util.type;

import com.google.common.reflect.TypeToken;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TypeTokens {
    public static class IntegerTypeToken extends TypeToken<Integer> {}

    public static class MutableBoundedValueIntegerTypeToken extends TypeToken<MutableBoundedValue<Integer>> {}

    public static class LongTypeToken extends TypeToken<Long> {}

    public static class LongValueTypeToken extends TypeToken<Value<Long>> {}

    public static class BlockFilterListTypeToken extends TypeToken<List<BlockFilter>> {}

    public static class ItemStackListTypeToken extends TypeToken<List<ItemStack>> {}

    public static class UUIDListTypeToken extends TypeToken<List<UUID>> {}

    public static class VariableTypeToken extends TypeToken<Map<String, Map<String, String>>> {}
}
