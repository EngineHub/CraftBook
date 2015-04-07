// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

import java.util.HashMap;
import java.util.Map;

/**
 * List of block IDs.
 *
 * @author sk89q
 */
public final class BlockType {

    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int GRASS = 2;
    public static final int DIRT = 3;
    public static final int COBBLESTONE = 4;
    public static final int WOOD = 5;
    public static final int SAPLING = 6;
    public static final int BEDROCK = 7;
    public static final int WATER = 8;
    public static final int STATIONARY_WATER = 9;
    public static final int LAVA = 10;
    public static final int STATIONARY_LAVA = 11;
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int GOLD_ORE = 14;
    public static final int IRON_ORE = 15;
    public static final int COAL_ORE = 16;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int SPONGE = 19;
    public static final int GLASS = 20;
    public static final int CLOTH = 35;
    public static final int YELLOW_FLOWER = 37;
    public static final int RED_FLOWER = 38;
    public static final int BROWN_MUSHROOM = 39;
    public static final int RED_MUSHROOM = 40;
    public static final int GOLD_BLOCK = 41;
    public static final int IRON_BLOCK = 42;
    public static final int DOUBLE_STEP = 43;
    public static final int STEP = 44;
    public static final int BRICK = 45;
    public static final int TNT = 46;
    public static final int BOOKCASE = 47;
    public static final int MOSSY_COBBLESTONE = 48;
    public static final int OBSIDIAN = 49;
    public static final int TORCH = 50;
    public static final int FIRE = 51;
    public static final int MOB_SPAWNER = 52;
    public static final int WOODEN_STAIRS = 53;
    public static final int CHEST = 54;
    public static final int REDSTONE_WIRE = 55;
    public static final int DIAMOND_ORE = 56;
    public static final int DIAMOND_BLOCK = 57;
    public static final int WORKBENCH = 58;
    public static final int CROPS = 59;
    public static final int SOIL = 60;
    public static final int FURNACE = 61;
    public static final int BURNING_FURNACE = 62;
    public static final int SIGN_POST = 63;
    public static final int WOODEN_DOOR = 64;
    public static final int LADDER = 65;
    public static final int MINECART_TRACKS = 66;
    public static final int COBBLESTONE_STAIRS = 67;
    public static final int WALL_SIGN = 68;
    public static final int LEVER = 69;
    public static final int STONE_PRESSURE_PLATE = 70;
    public static final int IRON_DOOR = 71;
    public static final int WOODEN_PRESSURE_PLATE = 72;
    public static final int REDSTONE_ORE = 73;
    public static final int GLOWING_REDSTONE_ORE = 74;
    public static final int REDSTONE_TORCH_OFF = 75;
    public static final int REDSTONE_TORCH_ON = 76;
    public static final int STONE_BUTTON = 77;
    public static final int SNOW = 78;
    public static final int ICE = 79;
    public static final int SNOW_BLOCK = 80;
    public static final int CACTUS = 81;
    public static final int CLAY = 82;
    public static final int REED = 83;
    public static final int JUKEBOX = 84;
    public static final int FENCE = 85;
    public static final int PUMPKIN = 86;
    public static final int NETHERSTONE = 87;
    public static final int SLOW_SAND = 88;
    public static final int LIGHTSTONE = 89;
    public static final int PORTAL = 90;
    public static final int JACKOLANTERN = 91;

    /**
     * Stores a list of dropped blocks for blocks.
     */
    private static final Map<Integer, Integer> blockDrops = new HashMap<Integer, Integer>();

    /**
     * Static constructor.
     */
    static {
        blockDrops.put(1, 4);
        blockDrops.put(2, 5);
        blockDrops.put(3, 3);
        blockDrops.put(4, 4);
        blockDrops.put(5, 5);
        blockDrops.put(6, 6);
        blockDrops.put(7, -1);
        blockDrops.put(12, 12);
        blockDrops.put(13, 13);
        blockDrops.put(14, 14);
        blockDrops.put(15, 15);
        blockDrops.put(16, 16);
        blockDrops.put(17, 17);
        blockDrops.put(18, 18);
        blockDrops.put(19, 19);
        blockDrops.put(35, 35);
        blockDrops.put(37, 37);
        blockDrops.put(38, 38);
        blockDrops.put(39, 39);
        blockDrops.put(40, 40);
        blockDrops.put(41, 41);
        blockDrops.put(42, 42);
        blockDrops.put(43, 43);
        blockDrops.put(44, 44);
        blockDrops.put(45, 45);
        blockDrops.put(47, 47);
        blockDrops.put(48, 48);
        blockDrops.put(49, 49);
        blockDrops.put(50, 50);
        blockDrops.put(53, 53);
        blockDrops.put(54, 54);
        blockDrops.put(55, 331);
        blockDrops.put(56, 56);
        blockDrops.put(57, 57);
        blockDrops.put(58, 58);
        blockDrops.put(59, 295);
        blockDrops.put(60, 60);
        blockDrops.put(61, 61);
        blockDrops.put(62, 61);
        blockDrops.put(63, 323);
        blockDrops.put(64, 324);
        blockDrops.put(65, 65);
        blockDrops.put(66, 66);
        blockDrops.put(67, 67);
        blockDrops.put(68, 323);
        blockDrops.put(69, 69);
        blockDrops.put(70, 70);
        blockDrops.put(71, 330);
        blockDrops.put(72, 72);
        blockDrops.put(73, 331);
        blockDrops.put(74, 331);
        blockDrops.put(75, 76);
        blockDrops.put(76, 76);
        blockDrops.put(77, 77);
        blockDrops.put(80, 80);
        blockDrops.put(81, 81);
        blockDrops.put(82, 82);
        blockDrops.put(83, 83);
        blockDrops.put(84, 84);
        blockDrops.put(85, 85);
        blockDrops.put(86, 86);
        blockDrops.put(87, 87);
        blockDrops.put(88, 88);
        blockDrops.put(89, 248);
        blockDrops.put(91, 91);
    }

    /**
     * Returns true if the block type requires a block underneath.
     *
     * @param id
     *
     * @return
     */
    public static boolean isBottomDependentBlock(int id) {

        return id == SAPLING
                || id == YELLOW_FLOWER
                || id == RED_FLOWER
                || id == BROWN_MUSHROOM
                || id == RED_MUSHROOM
                || id == TORCH
                || id == REDSTONE_WIRE
                || id == CROPS
                || id == SIGN_POST
                || id == WALL_SIGN
                || id == MINECART_TRACKS
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == STONE_BUTTON;
    }

    /**
     * Checks to see whether a block should be placed last.
     *
     * @param id
     *
     * @return
     */
    public static boolean shouldPlaceLast(int id) {

        return id == SAPLING
                || id == YELLOW_FLOWER
                || id == RED_FLOWER
                || id == BROWN_MUSHROOM
                || id == RED_MUSHROOM
                || id == TORCH
                || id == FIRE
                || id == REDSTONE_WIRE
                || id == CROPS
                || id == SIGN_POST
                || id == WOODEN_DOOR
                || id == LADDER
                || id == MINECART_TRACKS
                || id == WALL_SIGN
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == IRON_DOOR
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == STONE_BUTTON
                || id == SNOW
                || id == CACTUS
                || id == REED
                || id == PORTAL;
    }

    /**
     * Checks whether a block can be passed through.
     *
     * @param id
     *
     * @return
     */
    public static boolean canPassThrough(int id) {

        return id == AIR
                || id == SAPLING
                || id == YELLOW_FLOWER
                || id == RED_FLOWER
                || id == BROWN_MUSHROOM
                || id == RED_MUSHROOM
                || id == TORCH
                || id == FIRE
                || id == REDSTONE_WIRE
                || id == CROPS
                || id == SIGN_POST
                || id == LADDER
                || id == MINECART_TRACKS
                || id == WALL_SIGN
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == STONE_BUTTON
                || id == SNOW
                || id == REED
                || id == PORTAL;
    }

    /**
     * Returns true if the block uses its data value.
     *
     * @param id
     *
     * @return
     */
    public static boolean usesData(int id) {

        return id == SAPLING
                || id == WATER
                || id == STATIONARY_WATER
                || id == LAVA
                || id == STATIONARY_LAVA
                || id == TORCH
                || id == WOODEN_STAIRS
                || id == REDSTONE_WIRE
                || id == CROPS
                || id == SOIL
                || id == SIGN_POST
                || id == WOODEN_DOOR
                || id == LADDER
                || id == MINECART_TRACKS
                || id == COBBLESTONE_STAIRS
                || id == WALL_SIGN
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == IRON_DOOR
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == STONE_BUTTON
                || id == CACTUS;
    }

    /**
     * Returns true if an ID is lava.
     *
     * @param id
     *
     * @return
     */
    public static boolean isLava(int id) {

        return id == STATIONARY_LAVA
                || id == LAVA;
    }

    /**
     * Returns true if a block uses redstone in some way.
     *
     * @param id
     *
     * @return
     */
    public static boolean isRedstoneBlock(int id) {

        return id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_ON
                || id == REDSTONE_TORCH_OFF
                || id == STONE_BUTTON
                || id == REDSTONE_WIRE
                || id == WOODEN_DOOR
                || id == IRON_DOOR;
    }

    /**
     * Get the block or item that would have been dropped. If nothing is
     * dropped, 0 will be returned. If the block should not be destroyed
     * (i.e. bedrock), -1 will be returned.
     *
     * @param id
     *
     * @return
     */
    public static int getDroppedBlock(int id) {

        Integer dropped = blockDrops.get(id);
        if (dropped == null) {
            return 0;
        }
        return dropped;
    }
}
