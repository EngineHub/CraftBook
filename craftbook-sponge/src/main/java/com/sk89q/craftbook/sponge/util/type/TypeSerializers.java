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
package com.sk89q.craftbook.sponge.util.type;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.sk89q.craftbook.core.util.TernaryState;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.spongepowered.api.block.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeSerializers {

    public static void registerDefaults() {
        register(ninja.leaping.configurate.objectmapping.serialize.TypeSerializers.getDefaultSerializers());
    }

    public static void register(TypeSerializerCollection serializers) {
        serializers.registerType(TypeToken.of(BlockFilter.class), new TypeSerializers.BlockFilterTypeSerializer());
        //options.getSerializers().registerType(TypeToken.of(BlockState.class), new BlockStateTypeSerializer());
        serializers.registerType(new TypeToken<Set<?>>(){}, new SetTypeSerializer());
        //options.getSerializers().registerType(TypeToken.of(ItemStack.class), new ItemStackTypeSerializer());
        serializers.registerType(TypeToken.of(TernaryState.class), new TernaryStateTypeSerializer());
    }

    private static class BlockStateTypeSerializer implements TypeSerializer<BlockState> {
        @Override
        public BlockState deserialize(TypeToken<?> type, ConfigurationNode value) {
            return BlockUtil.getBlockStateFromString(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, BlockState obj, ConfigurationNode value) {
            value.setValue(obj.toString());
        }
    }

    private static class TernaryStateTypeSerializer implements TypeSerializer<TernaryState> {
        @Override
        public TernaryState deserialize(TypeToken<?> type, ConfigurationNode value) {
            return TernaryState.getFromString(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, TernaryState obj, ConfigurationNode value) {
            value.setValue(obj.name());
        }
    }

    private static class BlockFilterTypeSerializer implements TypeSerializer<BlockFilter> {
        @Override
        public BlockFilter deserialize(TypeToken<?> type, ConfigurationNode value) {
            return new BlockFilter(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, BlockFilter obj, ConfigurationNode value) {
            value.setValue(obj.getRule());
        }
    }

    private static class SetTypeSerializer implements TypeSerializer<Set<?>> {
        @Override
        public Set<?> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            TypeToken<?> entryType = type.resolveType(Set.class.getTypeParameters()[0]);
            TypeSerializer<?> entrySerial = value.getOptions().getSerializers().get(entryType);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryType);
            }

            if (value.hasListChildren()) {
                List<? extends ConfigurationNode> values = value.getChildrenList();
                Set<Object> ret = new HashSet<>(values.size());
                for (ConfigurationNode ent : values) {
                    ret.add(entrySerial.deserialize(entryType, ent));
                }
                return ret;
            } else {
                Object unwrappedVal = value.getValue();
                if (unwrappedVal != null) {
                    return Sets.newHashSet(entrySerial.deserialize(entryType, value));
                }
            }
            return new HashSet<>();
        }

        @Override
        public void serialize(TypeToken<?> type, Set<?> obj, ConfigurationNode value) throws ObjectMappingException {
            TypeToken<?> entryType = type.resolveType(Set.class.getTypeParameters()[0]);
            TypeSerializer entrySerial = value.getOptions().getSerializers().get(entryType);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryType);
            }
            value.setValue(ImmutableSet.of());
            for (Object ent : obj) {
                entrySerial.serialize(entryType, ent, value.getAppendedNode());
            }
        }
    }
}
