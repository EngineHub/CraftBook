package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

public class CompactId {

    private static final UUID[] knownWorldIds = new UUID[8];

    public static long computeWorldfulChunkId(World world, int chunkX, int chunkZ) {
        var worldId = getKnownWorldId(world.getUID());

        // x/z in [-30M;30M] => chunk x/z in [-1.875M;+1.875M], adding 1.875M will result in [0;3.75M], 22 bits

        // <17b unused><3b world_id><22b chunk_x><22b chunk_z>

        // 2^22 - 1 = 0x3FFFFF
        // 2^3 - 1 = 0x7
        return (
            (((chunkX + 1_875_000L) & 0x3FFFFF) << 22)
                | ((chunkZ + 1_875_000L) & 0x3FFFFF)
                | (((long) worldId & 0x7) << (22 * 2))
        );
    }

    public static long computeWorldfulBlockId(Block block) {
        var worldId = getKnownWorldId(block.getWorld().getUID());

        // <3b world_id><26b z><26b x><9b y>

        return computeWorldlessBlockId(block) | (((long) worldId & 0x7) << (9 + 26 * 2));
    }

    public static long computeWorldlessBlockId(Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        // y in [-64;320], adding 64 will result in [0;384], 9 bits
        // x/z in [-30M;30M], adding 30M will result in [0;60M], 26 bits

        // <3b zeroes><26b z><26b x><9b y>

        return (
            // 2^9 - 1 = 0x1FF
            // 2^26 - 1 = 0x3FFFFFF
            // 2^3 - 1 = 0x7
            ((y + 64) & 0x1FF) |
                (((x + 30_000_000L) & 0x3FFFFFF) << 9) |
                (((z + 30_000_000L) & 0x3FFFFFF) << (9 + 26))
        );
    }

    private static int getKnownWorldId(UUID worldId) {
        for (int i = 0; i < knownWorldIds.length; ++i) {
            UUID currentId = knownWorldIds[i];

            if (currentId == null) {
                knownWorldIds[i] = worldId;
                return i;
            }

            if (worldId.equals(currentId))
                return i;
        }

        // In practise, this should be unreachable.
        CraftBookPlugin.inst().getLogger().severe("Exceeded eight parallel worlds that use pipes!");
        return 0;
    }
}
