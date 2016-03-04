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
package com.sk89q.craftbook.sponge.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

import java.util.HashSet;
import java.util.Set;

public class TypeSerializers {

    public static void register(ConfigurationOptions options) {
        options.getSerializers().registerType(TypeToken.of(BlockState.class), new TypeSerializers.BlockStateTypeSerializer());
        options.getSerializers().registerType(TypeToken.of(BlockFilter.class), new TypeSerializers.BlockFilterTypeSerializer());
        options.getSerializers().registerType(new TypeToken<Set<?>>(){}, new TypeSerializers.SetTypeSerializer());
    }

    public static class BlockStateTypeSerializer implements TypeSerializer<BlockState> {
        @Override
        public BlockState deserialize(TypeToken<?> type, ConfigurationNode value) {
            return BlockTypes.AIR.getDefaultState();
        }

        @Override
        public void serialize(TypeToken<?> type, BlockState obj, ConfigurationNode value) {
            value.setValue(obj.toString());
        }
    }

    public static class BlockFilterTypeSerializer implements TypeSerializer<BlockFilter> {
        @Override
        public BlockFilter deserialize(TypeToken<?> type, ConfigurationNode value) {
            return new BlockFilter(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, BlockFilter obj, ConfigurationNode value) {
            value.setValue(obj.getRule());
        }
    }

    public static class SetTypeSerializer implements TypeSerializer<Set<?>> {
        @Override
        public Set<?> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            TypeToken<?> entryType = type.resolveType(Set.class.getTypeParameters()[0]);
            TypeSerializer<?> entrySerial = value.getOptions().getSerializers().get(entryType);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryType);
            }

            if (value.hasListChildren()) {
                Set<? extends ConfigurationNode> values = new HashSet<>(value.getChildrenList());
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
            value.setValue(ImmutableList.of());
            for (Object ent : obj) {
                entrySerial.serialize(entryType, ent, value.getAppendedNode());
            }
        }
    }
}
