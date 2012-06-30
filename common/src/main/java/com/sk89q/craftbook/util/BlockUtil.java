package com.sk89q.craftbook.util;

import org.bukkit.block.Block;

public class BlockUtil {

    public static boolean areBlocksSimilar(Block block, Block block2) {
	if(block.getTypeId() == block2.getTypeId()) return true;
	return false;
    }

    public static boolean areBlocksIdentical(Block block, Block block2) {
	if(block.getTypeId() == block2.getTypeId()) {
	    if(block.getData() == block2.getData()) {
		return true;
	    }
	}
	return false;
    }

    public static boolean isBlockSimilarTo(Block block, int type) {
	if(block.getTypeId() == type) return true;
	return false;
    }

    public static boolean isBlockIdenticalTo(Block block, int type, byte data) {
	if(block.getTypeId() == type) {
	    if(block.getData() == (byte) data) {
		return true;
	    }
	}
	return false;
    }
    
    public static void setBlockTypeAndData(Block block, int type, byte data) {
	block.setTypeIdAndData(type, data, true);
    }
}