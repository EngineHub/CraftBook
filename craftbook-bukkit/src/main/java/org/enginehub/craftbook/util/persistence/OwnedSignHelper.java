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

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A helper class to assign owners to signs.
 */
public class OwnedSignHelper {
    private static final NamespacedKey signOwnerKey = new NamespacedKey("craftbook", "owner");

    public static boolean hasOwner(Block block) {
        BlockState state = block.getState(false);
        return hasOwner(state);
    }

    public static boolean hasOwner(BlockState state) {
        if (state instanceof TileState tileState) {
            return tileState.getPersistentDataContainer().has(signOwnerKey, UuidPersistentDataType.UUID_PERSISTENT_DATA_TYPE);
        } else {
            return false;
        }
    }

    public static @Nullable UUID getOwner(Block block) {
        BlockState state = block.getState(false);
        return getOwner(state);
    }

    public static @Nullable UUID getOwner(BlockState state) {
        if (state instanceof TileState tileState) {
            return tileState.getPersistentDataContainer().get(signOwnerKey, UuidPersistentDataType.UUID_PERSISTENT_DATA_TYPE);
        } else {
            return null;
        }
    }

    public static void setOwner(TileState tileState, @Nullable UUID owner) {
        if (owner == null) {
            tileState.getPersistentDataContainer().remove(signOwnerKey);
        } else {
            tileState.getPersistentDataContainer().set(signOwnerKey, UuidPersistentDataType.UUID_PERSISTENT_DATA_TYPE, owner);
        }
    }
}
