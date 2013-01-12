// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.circuits.ic;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemType;

/**
 * IC utility functions.
 *
 * @author sk89q
 */
public class ICUtil {

    // private static BlockFace[] REDSTONE_CONTACT_FACES =
    // {BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP};

    public ICUtil() {

    }

    private static HashMap<Location, Boolean> torchStatus = new HashMap<Location, Boolean>();

    public static Boolean getTorchStatus(Location loc) {

        return torchStatus.get(loc);
    }

    public static void removeTorch(Location loc) {

        torchStatus.remove(loc);
    }

    public static void setTorch(Location loc, Boolean value) {

        torchStatus.put(loc, value);
    }

    /**
     * Set an IC's output state at a block.
     *
     * @param block
     * @param state
     *
     * @return whether something was changed
     */
    public static boolean setState(Block block, boolean state, Block source) {

        if (block.getTypeId() != BlockID.LEVER) return false;

        // return if the lever is not attached to our IC block
        Lever lever = (Lever) block.getState().getData();
        if (!block.getRelative(lever.getAttachedFace()).equals(source)) {
            return false;
        }

        // check if the lever was toggled on
        boolean wasOn = (block.getData() & 0x8) > 0;

        byte data = block.getData();
        int newData;
        // check if the state changed and set the data value
        if (!state) {
            newData = data & 0x7;
        } else {
            newData = data | 0x8;
        }

        // if the state changed lets apply physics to the source block and the lever itself
        if (wasOn != state) {

            // set the new data
            block.setData((byte) newData, true);
            // apply physics to the source block the lever is attached to
            block.setData(block.getData(), true);
            BlockRedstoneEvent event = new BlockRedstoneEvent(block,wasOn ? 15 : 0, state ? 15 : 0);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);
            return true;
        }

        return false;
    }

    public static Block parseBlockLocation(ChangedSign sign, int lPos, LocationCheckType relative) {

        Block target = SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock());
        String line = sign.getLine(lPos);
        if (line.contains("!"))
            line = line.replace("!", "");
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        if (line.contains("=")) {
            String[] split = RegexUtil.EQUALS_PATTERN.split(line);
            line = split[1];
        }
        try {
            String[] split = RegexUtil.COLON_PATTERN.split(line);
            if (split.length > 1) {
                offsetX = Integer.parseInt(split[0]);
                offsetY = Integer.parseInt(split[1]);
                offsetZ = Integer.parseInt(split[2]);
            } else {
                offsetY = Integer.parseInt(line);
            }
        } catch (NumberFormatException e) {
            // do nothing and use defaults
        } catch (ArrayIndexOutOfBoundsException e) {
            // do nothing and use defaults
        }
        if (relative == LocationCheckType.RELATIVE) {
            target = LocationUtil.getRelativeOffset(sign, offsetX, offsetY, offsetZ);
        } else if (relative == LocationCheckType.OFFSET){
            target = LocationUtil.getOffset(target, offsetX, offsetY, offsetZ);
        } else {
            target = new Location(target.getWorld(), offsetX, offsetY, offsetZ).getBlock();
        }
        return target;
    }

    public static Block parseBlockLocation(ChangedSign sign, int lPos) {

        return parseBlockLocation(sign, lPos, LocationCheckType.RELATIVE);
    }

    public static Block parseBlockLocation(ChangedSign sign) {

        return parseBlockLocation(sign, 2, LocationCheckType.RELATIVE);
    }

    public static void verifySignSyntax(ChangedSign sign) throws ICVerificationException {

        verifySignSyntax(sign, 2);
    }

    public static void verifySignSyntax(ChangedSign sign, int i) throws ICVerificationException {

        try {
            String line = sign.getLine(i);
            String[] strings;
            if (line.contains("=")) {
                String[] split = RegexUtil.EQUALS_PATTERN.split(line, 2);
                Integer.parseInt(split[0]);
                strings = RegexUtil.COLON_PATTERN.split(split[1], 3);
            } else {
                strings = RegexUtil.COLON_PATTERN.split(line);
            }
            if (strings.length > 1) {
                Integer.parseInt(strings[1]);
                Integer.parseInt(strings[2]);
            }
            Integer.parseInt(strings[0]);
        } catch (Exception e) {
            throw new ICVerificationException("Wrong syntax! Needs to be: radius=x:y:z or radius=y or y");
        }
    }

    public static int parseRadius(ChangedSign sign) {

        return parseRadius(sign, 2);
    }

    public static int parseRadius(ChangedSign sign, int lPos) {

        String line = sign.getLine(lPos);
        int radius = 10; // default radius is 10.
        try {
            return Integer.parseInt(RegexUtil.EQUALS_PATTERN.split(line, 2)[0]);
        } catch (NumberFormatException e) {
            // do nothing and use default radius
        }
        return radius;
    }

    public static ItemStack getItem(String line) {

        if (line == null || line.isEmpty()) {
            return null;
        }
        try {
            if (line.contains(":")) {
                String[] split = RegexUtil.COLON_PATTERN.split(line, 2);
                int id = 0;
                int data = 0;
                try {
                    id = Integer.parseInt(split[0]);
                    data = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    try {
                        id = BlockType.lookup(split[0]).getID();
                        if (id < 0) throw new NullPointerException();
                    } catch (Exception ee) {
                        id = ItemType.lookup(split[0]).getID();
                    }
                    data = Integer.parseInt(split[1]);
                }
                return new ItemStack(id, 1, (short) data);
            } else {
                int id = 0;
                try {
                    id = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    try {
                        id = BlockType.lookup(line).getID();
                        if (id < 0) throw new NullPointerException();
                    } catch (Exception ee) {
                        id = ItemType.lookup(line).getID();
                    }
                }
                return new ItemStack(id, 1, (short) 0);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public enum LocationCheckType {

        RELATIVE,
        OFFSET,
        ABSOLUTE;
    }
}
