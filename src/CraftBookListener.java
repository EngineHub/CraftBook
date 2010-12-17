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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CraftBookListener extends PluginListener {
    /**
     * Logger.
     */
    static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    private boolean rsLock = false;
    
    /**
     * The block that was changed.
     */
    private BlockVector changedRedstoneInput;

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    public void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min) {
            throw new InsufficientArgumentsException("Minimum " + min + " arguments");
        } else if (max != -1 && args.length - 1 > max) {
            throw new InsufficientArgumentsException("Maximum " + max + " arguments");
        }
    }

    /*
    * Called whenever a redstone source (wire, switch, torch) changes its
    * current.
    *
    * Standard values for wires are 0 for no current, and 14 for a strong current.
    * Default behaviour for redstone wire is to lower the current by one every
    * block.
    *
    * For other blocks which provide a source of redstone current, the current
    * value will be 1 or 0 for on and off respectively.
    *
    * @param redstone Block of redstone which has just changed in current
    * @param oldLevel the old current
    * @param newLevel the new current
    */
    public int onRedstoneChange(Block block, int oldLevel, int newLevel) {
        return onRedstoneChange(new BlockVector(block.getX(),block.getY(),block.getZ()),oldLevel,newLevel);
    }
    
    /**
     * Called on redstone input change. This is passed a BlockVector.
     * 
     * @param v
     * @param oldLevel
     * @param newLevel
     * @return
     */
    public int onRedstoneChange(BlockVector v, int oldLevel, int newLevel) {
        if (rsLock) {
            craftBook.getDelay().delayRsChange(v, oldLevel, newLevel);
            return newLevel;
        }

        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) {
            return newLevel;
        }

        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();

        int type = CraftBook.getBlockID(x, y, z);
        //Unused
        //int above = CraftBook.getBlockID(x, y + 1, z);

        changedRedstoneInput = new BlockVector(x, y, z);

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly
        try {
            if (type == BlockType.LEVER) {
                // Fake data
                CraftBook.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBook.getBlockData(x, y, z) | 0x8
                            : CraftBook.getBlockData(x, y, z) & 0x7);
            } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                // Fake data
                CraftBook.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBook.getBlockData(x, y, z) | 0x1
                            : CraftBook.getBlockData(x, y, z) & 0x14);
            } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                // Fake data
                CraftBook.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBook.getBlockData(x, y, z) | 0x1
                            : CraftBook.getBlockData(x, y, z) & 0x14);
            } else if (type == BlockType.STONE_BUTTON) {
                // Fake data
                CraftBook.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBook.getBlockData(x, y, z) | 0x8
                            : CraftBook.getBlockData(x, y, z) & 0x7);
            } else if (type == BlockType.REDSTONE_WIRE) {
                // Fake data
                CraftBook.fakeBlockData(x, y, z, newLevel);

                int westSide = CraftBook.getBlockID(x, y, z + 1);
                int westSideAbove = CraftBook.getBlockID(x, y + 1, z + 1);
                int westSideBelow = CraftBook.getBlockID(x, y - 1, z + 1);
                int eastSide = CraftBook.getBlockID(x, y, z - 1);
                int eastSideAbove = CraftBook.getBlockID(x, y + 1, z - 1);
                int eastSideBelow = CraftBook.getBlockID(x, y - 1, z - 1);

                int northSide = CraftBook.getBlockID(x - 1, y, z);
                int northSideAbove = CraftBook.getBlockID(x - 1, y + 1, z);
                int northSideBelow = CraftBook.getBlockID(x - 1, y - 1, z);
                int southSide = CraftBook.getBlockID(x + 1, y, z);
                int southSideAbove = CraftBook.getBlockID(x + 1, y + 1, z);
                int southSideBelow = CraftBook.getBlockID(x + 1, y - 1, z);

                // Make sure that the wire points to only this block
                if (!BlockType.isRedstoneBlock(westSide)
                        && !BlockType.isRedstoneBlock(eastSide)
                        && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0)
                        && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0)
                        && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                        && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                    // Possible blocks north / south
                    handleDirectWireInput(new Vector(x - 1, y, z), isOn);
                    handleDirectWireInput(new Vector(x + 1, y, z), isOn);
                }

                if (!BlockType.isRedstoneBlock(northSide)
                        && !BlockType.isRedstoneBlock(southSide)
                        && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0)
                        && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0)
                        && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                        && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                    // Possible blocks west / east
                    handleDirectWireInput(new Vector(x, y, z - 1), isOn);
                    handleDirectWireInput(new Vector(x, y, z + 1), isOn);
                }

                // Can be triggered from below
                handleDirectWireInput(new Vector(x, y + 1, z), isOn);

                return newLevel;
            }

            // For redstone wires, the code already exited this method
            // Non-wire blocks proceed

            handleDirectWireInput(new Vector(x - 1, y, z), isOn);
            handleDirectWireInput(new Vector(x + 1, y, z), isOn);
            handleDirectWireInput(new Vector(x, y, z - 1), isOn);
            handleDirectWireInput(new Vector(x, y, z + 1), isOn);

            // Can be triggered from below
            handleDirectWireInput(new Vector(x, y + 1, z), isOn);

            return newLevel;
        } finally {
            CraftBook.clearFakeBlockData();
        }
    }

    public void setRsLock(boolean value) {
        rsLock = value;
    }

    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        try {
            return runCommand(player, split);
        } catch (InsufficientArgumentsException e) {
            player.sendMessage(Colors.Rose + e.getMessage());
            return true;
        } catch (LocalWorldEditBridgeException e) {
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                if (cause.getClass().getCanonicalName().equals("com.sk89q.worldedit.IncompleteRegionException")) {
                    player.sendMessage(Colors.Rose + "Region not fully defined (via WorldEdit).");
                } else if (cause.getClass().getCanonicalName().equals("com.sk89q.worldedit.WorldEditNotInstalled")) {
                    player.sendMessage(Colors.Rose + "The WorldEdit plugin is not loaded.");
                } else {
                    player.sendMessage(Colors.Rose + "Unknown CraftBook<->WorldEdit error: " + cause.getClass().getCanonicalName());
                }
            } else {
                player.sendMessage(Colors.Rose + "Unknown CraftBook<->WorldEdit error: " + e.getMessage());
            }

            return true;
        }
    }
    
    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    public boolean runCommand(Player player, String[] split)
            throws InsufficientArgumentsException, LocalWorldEditBridgeException {        
        if (split[0].equalsIgnoreCase("/reload") && canUse(player, "/reload")) {
            loadConfiguration();
        }

        return false;
    }

    /**
     * Get a block bag.
     * 
     * @param origin
     * @return
     */
    public BlockSource getBlockSource(Vector origin) {
        List<BlockSource> bags = new ArrayList<BlockSource>();
        for (BlockSourceFactory f : blockSources) {
            BlockSource b = f.createBlockSource(origin);
            if (b == null) {
            	continue;
            }
            bags.add(b);
        }
        return new CompoundBlockSource(bags);
    }

    /**
     * Check if a player can use a command.
     *
     * @param player
     * @param command
     * @return
     */
    public boolean canUse(Player player, String command) {
        return player.canUseCommand(command);
    }

    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean checkPermission(Player player, String command) {
        return !checkPermissions || player.canUseCommand(command);
    }
}