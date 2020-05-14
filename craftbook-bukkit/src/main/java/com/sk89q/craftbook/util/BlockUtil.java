/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Snow;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockUtil {

    private BlockUtil() {
    }

    public static boolean areBlocksSimilar(Block block, Block block2) {
        return block.getType() == block2.getType();
    }

    public static boolean areBlocksIdentical(Block block, Block block2) {
        return block.getType() == block2.getType() && block.getBlockData().matches(block2.getBlockData());
    }

    public static boolean areBlocksIdentical(Block block, BlockData block2) {
        return block.getType() == block2.getMaterial() && block.getBlockData().matches(block2);
    }

    public static boolean isBlockSimilarTo(Block block, Material type) {
        return block.getType() == type;
    }

    public static boolean isBlockReplacable(Material id) {
        switch (id) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
            case DEAD_BUSH:
            case FIRE:
            case GRASS:
            case FERN:
            case LAVA:
            case WATER:
            case SNOW:
            case MOVING_PISTON:
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

        List<ItemStack> drops = new ArrayList<>();

        switch(block.getType()) {
            case SNOW:
                if(tool == null) break;
                if(tool.getType() == Material.WOODEN_SHOVEL
                        || tool.getType() == Material.STONE_SHOVEL
                        || tool.getType() == Material.IRON_SHOVEL
                        || tool.getType() == Material.GOLDEN_SHOVEL
                        || tool.getType() == Material.DIAMOND_SHOVEL)
                    drops.add(new ItemStack(Material.SNOWBALL, ((Snow) block.getBlockData()).getLayers() + 1));
                break;
            case WHEAT: {
                drops.add(new ItemStack(Material.WHEAT, 1));
                int amount = ThreadLocalRandom.current().nextInt(4);
                if (amount > 0)
                    drops.add(new ItemStack(Material.WHEAT_SEEDS, amount));
                break;
            }
            case BEETROOTS:
                drops.add(new ItemStack(Material.BEETROOT, 1));
                int amount = ThreadLocalRandom.current().nextInt(4);
                if(amount > 0)
                    drops.add(new ItemStack(Material.BEETROOT_SEEDS, amount));
                break;
            case CARROTS:
                drops.add(new ItemStack(Material.CARROT, 1 + ThreadLocalRandom.current().nextInt(4)));
                break;
            case POTATOES:
                drops.add(new ItemStack(Material.POTATO, 1 + ThreadLocalRandom.current().nextInt(4)));
                if(ThreadLocalRandom.current().nextInt(50) == 0)
                    drops.add(new ItemStack(Material.POISONOUS_POTATO, 1));
                break;
            case NETHER_WART:
                drops.add(new ItemStack(Material.NETHER_WART, 2 + ThreadLocalRandom.current().nextInt(3)));
                break;
            case SUGAR_CANE:
                drops.add(new ItemStack(Material.SUGAR_CANE, 1));
                break;
            case MELON:
                drops.add(new ItemStack(Material.MELON_SLICE, 3 + ThreadLocalRandom.current().nextInt(5)));
                break;
            case COCOA:
                drops.add(new ItemStack(Material.COCOA_BEANS, 3));
                break;
            default:
                if(tool == null) {
                    drops.addAll(block.getDrops());
                } else {
                    drops.addAll(block.getDrops(tool));
                }
                break;
        }

        return drops.toArray(new ItemStack[0]);
    }

    public static Block[] getTouchingBlocks(Block block) {

        List<Block> blocks = new ArrayList<>();
        for(BlockFace face : LocationUtil.getDirectFaces())
            blocks.add(block.getRelative(face));

        return blocks.toArray(new Block[0]);
    }

    public static Block[] getIndirectlyTouchingBlocks(Block block) {

        List<Block> blocks = new ArrayList<>();
        for(int x = -1; x < 2; x++)
            for(int y = -1; y < 2; y++)
                for(int z = -1; z < 2; z++)
                    if(!(x == 0 && y == 0 & z == 0))
                        blocks.add(block.getRelative(x,y,z));

        return blocks.toArray(new Block[0]);
    }
}
