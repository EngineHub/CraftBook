package com.sk89q.craftbook.util;

import org.bukkit.block.Block;

public class BlockUtil {

    public static boolean areBlocksSimilar(Block block, Block block2) {

        return block.getTypeId() == block2.getTypeId();
    }

    public static boolean areBlocksIdentical(Block block, Block block2) {

        if (block.getTypeId() == block2.getTypeId()) {
            if (block.getData() == block2.getData()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockSimilarTo(Block block, int type) {

        return block.getTypeId() == type;
    }

    public static boolean isBlockIdenticalTo(Block block, int type, byte data) {

        if (block.getTypeId() == type) {
            if (block.getData() == data) {
                return true;
            }
        }
        return false;
    }

    public static void setBlockTypeAndData(Block block, int type, byte data) {

        block.setTypeIdAndData(type, data, true);
    }
}