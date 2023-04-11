/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.util.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility class to store a UUID by most & least significant bits within a persistent data container.
 */
public class UuidPersistentDataType implements PersistentDataType<byte[], UUID> {
    public static final UuidPersistentDataType UUID_PERSISTENT_DATA_TYPE = new UuidPersistentDataType();

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
        // 128 (size of two longs) / 8 (size of byte) = 16
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(complex.getMostSignificantBits());
        buffer.putLong(complex.getLeastSignificantBits());
        return buffer.array();
    }

    @Override
    public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer buffer = ByteBuffer.wrap(primitive);
        long mostSig = buffer.getLong();
        long leastSig = buffer.getLong();
        return new UUID(mostSig, leastSig);
    }
}
