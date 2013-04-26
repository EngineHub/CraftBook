package com.sk89q.craftbook.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.sk89q.worldedit.blocks.BlockID;

public class BlockUtil {

    public static boolean areBlocksSimilar(Block block, Block block2) {

        return block.getTypeId() == block2.getTypeId();
    }

    public static boolean areBlocksIdentical(Block block, Block block2) {

        if (block.getTypeId() == block2.getTypeId()) if (block.getData() == block2.getData()) return true;
        return false;
    }

    public static boolean isBlockSimilarTo(Block block, int type) {

        return block.getTypeId() == type;
    }

    public static boolean isBlockIdenticalTo(Block block, int type, byte data) {

        if (block.getTypeId() == type) if (block.getData() == data) return true;
        return false;
    }

    public static boolean isBlockSolid(int id) {

        switch (id) {

            case BlockID.AIR:
            case BlockID.CROPS:
            case BlockID.DEAD_BUSH:
            case BlockID.END_PORTAL:
            case BlockID.FIRE:
            case BlockID.GRASS:
            case BlockID.LAVA:
            case BlockID.STATIONARY_LAVA:
            case BlockID.WATER:
            case BlockID.STATIONARY_WATER:
                return false;
            default:
                return true;
        }
    }

    public static Location getBlockCentre(Block block) {

        return block.getLocation().add(0.5, 0.5, 0.5);
    }
}