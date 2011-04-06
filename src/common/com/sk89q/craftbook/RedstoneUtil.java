package com.sk89q.craftbook;

import org.bukkit.block.*;

/**
 * Decorates bukkit's directional block power queries with a three-valued logic
 * that differenciates between the wiring that is unpowered and the absense of
 * wiring.
 * 
 * @author hash
 * 
 */
public abstract class RedstoneUtil {
    /**
     * @param block 
     * @param face
     * @return Boolean.TRUE if power is supplied by the given face;
     *         Boolean.FALSE if there is a potential power source at the given
     *         face, but it is not providing power; null if there is no
     *         potential power source at the given face.
     */
    public static Boolean isBlockFacePowered(Block block, BlockFace face) {
        if (block.isBlockFacePowered(face)) return true;
        return com.sk89q.worldedit.blocks.BlockType.isRedstoneBlock(block.getFace(face).getTypeId()) ? Boolean.FALSE : null;
    }
}
