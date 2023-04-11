package org.enginehub.craftbook.util.persistence;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;

import java.util.UUID;
import javax.annotation.Nullable;

/**
 * A helper class to assign owners to signs.
 */
public class OwnedSignHelper {
    private static final NamespacedKey signOwnerKey = new NamespacedKey("craftbook", "owner");

    @Nullable
    public static UUID getOwner(Block block) {
        BlockState state = block.getState(false);
        return getOwner(state);
    }

    @Nullable
    public static UUID getOwner(BlockState state) {
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
