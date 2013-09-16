package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

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

    public static boolean isBlockReplacable(int id) {

        switch (id) {

            case BlockID.AIR:
            case BlockID.CROPS:
            case BlockID.DEAD_BUSH:
            case BlockID.END_PORTAL:
            case BlockID.FIRE:
            case BlockID.LONG_GRASS:
            case BlockID.LAVA:
            case BlockID.STATIONARY_LAVA:
            case BlockID.WATER:
            case BlockID.STATIONARY_WATER:
            case BlockID.VINE:
            case BlockID.SNOW:
            case BlockID.PISTON_MOVING_PIECE:
                return true;
            default:
                return false;
        }
    }

    public static Location getBlockCentre(Block block) {

        return block.getLocation().add(0.5, 0.5, 0.5);
    }

    /**
     * Gets a list of all drops for a particular block.
     * 
     * @param block The block to check drops for.
     * @param tool The tool. If null, it'll allow for all drops.
     * @return The list of drops
     */
    public static ItemStack[] getBlockDrops(Block block, ItemStack tool) {

        List<ItemStack> drops = new ArrayList<ItemStack>();

        switch(block.getTypeId()) {
            case BlockID.SNOW:
                if(tool == null || tool.getTypeId() == ItemID.WOOD_SHOVEL || tool.getTypeId() == ItemID.STONE_SHOVEL || tool.getTypeId() == ItemID.IRON_SHOVEL || tool.getTypeId() == ItemID.GOLD_SHOVEL || tool.getTypeId() == ItemID.DIAMOND_SHOVEL)
                    drops.add(new ItemStack(ItemID.SNOWBALL));
                break;
            case BlockID.CROPS:
                drops.add(new ItemStack(ItemID.WHEAT, 1));
                int amount = CraftBookPlugin.inst().getRandom().nextInt(4);
                if(amount > 0)
                    drops.add(new ItemStack(ItemID.SEEDS, amount));
                break;
            case BlockID.CARROTS:
                drops.add(new ItemStack(ItemID.CARROT, 1 + CraftBookPlugin.inst().getRandom().nextInt(4)));
                break;
            case BlockID.POTATOES:
                drops.add(new ItemStack(ItemID.POTATO, 1 + CraftBookPlugin.inst().getRandom().nextInt(4)));
                if(CraftBookPlugin.inst().getRandom().nextInt(50) == 0)
                    drops.add(new ItemStack(ItemID.POISONOUS_POTATO, 1));
                break;
            case BlockID.NETHER_WART:
                drops.add(new ItemStack(ItemID.NETHER_WART_SEED, 2 + CraftBookPlugin.inst().getRandom().nextInt(3)));
                break;
            case BlockID.REED:
                drops.add(new ItemStack(ItemID.SUGAR_CANE_ITEM, 1));
                break;
            case BlockID.MELON_BLOCK:
                drops.add(new ItemStack(ItemID.MELON, 3 + CraftBookPlugin.inst().getRandom().nextInt(5)));
                break;
            case BlockID.COCOA_PLANT:
                drops.add(new ItemStack(ItemID.INK_SACK, 3, (short) 3));
                break;
            default:
                if(tool == null)
                    drops.addAll(block.getDrops());
                else
                    drops.addAll(block.getDrops(tool));
                break;
        }

        return drops.toArray(new ItemStack[drops.size()]);
    }


    /**
     * Used for backwards compatability of block faces.
     */
    private static Boolean useOldBlockFace;


    /**
     * Check to see if it should use the old block face methods.
     *
     * @return whether it should use the old BlockFace methods.
     */
    public static boolean shouldUseOldFaces() {

        if (useOldBlockFace == null) {
            Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
            useOldBlockFace = loc.getBlock().getRelative(BlockFace.WEST).getX() == 0;
        }

        return useOldBlockFace;
    }
}