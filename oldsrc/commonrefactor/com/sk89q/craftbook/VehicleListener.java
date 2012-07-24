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

import com.sk89q.craftbook.access.*;
import com.sk89q.craftbook.blockbag.BlockBagException;
import com.sk89q.craftbook.blockbag.NearbyChestBlockBag;
import com.sk89q.craftbook.mech.MinecartDecayWatcher;
import com.sk89q.craftbook.util.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Delegate listener for vehicle-related hooks and features.
 *
 * @author sk89q
 */
public class VehicleListener extends CraftBookDelegateListener {

    /**
     * Station to stop at.
     */
    private Map<String, String> stopStation =
            new HashMap<String, String>();
    /**
     * Last minecart message sent from a sign to a player.
     */
    private Map<String, String> lastMinecartMsg =
            new HashMap<String, String>();
    /**
     * Last time minecart message sent from a sign to a player.
     */
    private Map<String, Long> lastMinecartMsgTime =
            new HashMap<String, Long>();

    /**
     * Decay watchers. Used to remove empty minecarts.
     */
    private MinecartDecayWatcher decayWatcher;

    /**
     * Track direction for sorting.
     */
    private enum SortDir {
        FORWARD, LEFT, RIGHT
    }

    // Settings
    private boolean minecartControlBlocks = true;
    private boolean slickPressurePlates = false;
    private boolean unoccupiedCoast = true;
    private boolean inCartControl = true;
    private boolean minecartDispensers = true;
    private boolean minecartTrackMessages = true;
    private int minecart25xBoostBlock = BlockType.GOLD_ORE;
    private int minecart100xBoostBlock = BlockType.GOLD_BLOCK;
    private int minecart50xSlowBlock = BlockType.SLOW_SAND;
    private int minecart20xSlowBlock = BlockType.GRAVEL;
    private int minecartStationBlock = BlockType.OBSIDIAN;
    private int minecartReverseBlock = BlockType.CLOTH;
    private int minecartDepositBlock = BlockType.IRON_ORE;
    private int minecartEjectBlock = BlockType.IRON_BLOCK;
    private int minecartSortBlock = BlockType.NETHERSTONE;
    private boolean minecartDestroyOnExit = false;
    private boolean minecartDropOnExit = false;

    /**
     * Construct the object.
     *
     * @param craftBook
     * @param listener
     * @param properties
     */
    public VehicleListener(CraftBookCore craftBook, ServerInterface server) {

        super(craftBook, server);
    }

    /**
     * /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {

        Configuration c = server.getConfiguration();

        minecartControlBlocks = c.getBoolean("minecart-control-blocks", true);
        slickPressurePlates = c.getBoolean("hinder-minecart-pressure-plate-slow", true);
        unoccupiedCoast = c.getBoolean("minecart-hinder-unoccupied-slowdown", true);
        inCartControl = c.getBoolean("minecart-in-cart-control", true);
        minecartDispensers = c.getBoolean("minecart-dispensers", true);
        minecartTrackMessages = c.getBoolean("minecart-track-messages", true);
        minecart25xBoostBlock = c.getInt("minecart-25x-boost-block", BlockType.GOLD_ORE);
        minecart100xBoostBlock = c.getInt("minecart-100x-boost-block", BlockType.GOLD_BLOCK);
        minecart50xSlowBlock = c.getInt("minecart-50x-slow-block", BlockType.SLOW_SAND);
        minecart20xSlowBlock = c.getInt("minecart-20x-slow-block", BlockType.GRAVEL);
        minecartStationBlock = c.getInt("minecart-station-block", BlockType.OBSIDIAN);
        minecartReverseBlock = c.getInt("minecart-reverse-block", BlockType.CLOTH);
        minecartDepositBlock = c.getInt("minecart-deposit-block", BlockType.IRON_ORE);
        minecartEjectBlock = c.getInt("minecart-eject-block", BlockType.IRON_BLOCK);
        minecartSortBlock = c.getInt("minecart-sort-block", BlockType.NETHERSTONE);

        int decay = c.getInt("minecart-decay-time", 0);

        decayWatcher = null;

        if (decay != 0) {
            decayWatcher = new MinecartDecayWatcher(decay);
        }

        minecartDestroyOnExit = c.getBoolean("minecart-destroy-on-exit", false);
        minecartDropOnExit = c.getBoolean("minecart-drop-on-exit", false);
    }

    /**
     * Called before the command is parsed. Return true if you don't want the
     * command to be parsed.
     *
     * @param player
     * @param split
     *
     * @return false if you want the command to be parsed.
     */
    public boolean onCommand(PlayerInterface player, String[] split) {
        // /st station stop command
        if (split[0].equalsIgnoreCase("/st")) {
            if (split.length >= 2) {
                stopStation.put(player.getName(), "#" + split[1].trim());
                player.sendMessage(Colors.GOLD + "You will stop at station \""
                        + split[1].trim() + "\".");
            } else {
                player.sendMessage(Colors.RED
                        + "You need to specify a station name.");
            }
            return true;
        }
        return false;
    }

    /**
     * Handles the wire input at a block in the case when the wire is
     * directly connected to the block in question only.
     *
     * @param x
     * @param y
     * @param z
     * @param isOn
     */
    public void onWireInput(WorldInterface world, Vector pt, boolean isOn, Vector changed) {

        int type = world.getId(pt);

        // Minecart dispenser
        if (minecartDispensers && type == BlockType.CHEST
                && (world.getId(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                || world.getId(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {

            // Rising edge-triggered only
            if (!isOn) {
                return;
            }

            Vector signPos = pt.add(0, -2, 0);

            SignInterface sign = getControllerSign(world, pt.add(0, -1, 0), "[Dispenser]");

            if (sign == null) {
                return;
            }

            String collectType = sign != null ? sign.getLine2() : "";
            boolean push = sign != null ? sign.getLine4().contains("Push") : false;

            Vector dir = MinecraftUtil.getSignPostOrthogonalBack(world, signPos, 1)
                    .subtract(signPos);
            Vector depositPt = pt.add(dir.multiply(2.5));

            /*if (CraftBook.getBlockID(depositPt) != BlockType.MINECART_TRACKS) {
                return;
            }*/

            NearbyChestBlockBag blockBag = new NearbyChestBlockBag(pt);
            blockBag.addSingleSourcePosition(world, pt);
            blockBag.addSingleSourcePosition(world, pt.add(1, 0, 0));
            blockBag.addSingleSourcePosition(world, pt.add(-1, 0, 0));
            blockBag.addSingleSourcePosition(world, pt.add(0, 0, 1));
            blockBag.addSingleSourcePosition(world, pt.add(0, 0, -1));

            try {
                MinecartInterface minecart;

                if (collectType.equalsIgnoreCase("Storage")) {
                    try {
                        blockBag.fetchBlock(ItemType.STORAGE_MINECART);
                    } catch (BlockBagException e) {
                        // Okay, no storage minecarts... but perhaps we can
                        // craft a minecart + chest!
                        if (blockBag.peekBlock(BlockType.CHEST)) {
                            blockBag.fetchBlock(ItemType.MINECART);
                            blockBag.fetchBlock(BlockType.CHEST);
                        } else {
                            throw new BlockBagException();
                        }
                    }

                    minecart = world.spawnMinecart(
                            depositPt.getX(),
                            depositPt.getY(),
                            depositPt.getZ(),
                            MinecartInterface.Type.STORAGE);
                } else if (collectType.equalsIgnoreCase("Powered")) {
                    try {
                        blockBag.fetchBlock(ItemType.POWERED_MINECART);
                    } catch (BlockBagException e) {
                        // Okay, no storage minecarts... but perhaps we can
                        // craft a minecart + chest!
                        if (blockBag.peekBlock(BlockType.FURNACE)) {
                            blockBag.fetchBlock(ItemType.MINECART);
                            blockBag.fetchBlock(BlockType.FURNACE);
                        } else {
                            throw new BlockBagException();
                        }
                    }

                    minecart = world.spawnMinecart(
                            depositPt.getX(),
                            depositPt.getY(),
                            depositPt.getZ(),
                            MinecartInterface.Type.POWERED);
                } else {
                    blockBag.fetchBlock(ItemType.MINECART);
                    minecart = world.spawnMinecart(
                            depositPt.getX(),
                            depositPt.getY(),
                            depositPt.getZ(),
                            MinecartInterface.Type.REGULAR);
                }

                if (push) {
                    int data = world.getData(signPos);

                    if (data == 0x0) {
                        minecart.setXSpeed(0);
                        minecart.setYSpeed(0);
                        minecart.setZSpeed(-0.3);
                    } else if (data == 0x4) {
                        minecart.setXSpeed(0.3);
                        minecart.setYSpeed(0);
                        minecart.setZSpeed(0);
                    } else if (data == 0x8) {
                        minecart.setXSpeed(0);
                        minecart.setYSpeed(0);
                        minecart.setZSpeed(0.3);
                    } else if (data == 0xC) {
                        minecart.setXSpeed(-0.3);
                        minecart.setYSpeed(0);
                        minecart.setZSpeed(0);
                    }
                }
            } catch (BlockBagException e) {
                // No minecarts
            }
            // Minecart station
        } else if (minecartControlBlocks && type == minecartStationBlock
                && world.getId(pt.add(0, 1, 0)) == BlockType.MINECART_TRACKS
                && (world.getId(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                || world.getId(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            BlockEntity cblock = world.getBlockEntity(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());

            // Maybe it's the sign directly below
            if (cblock == null || !(cblock instanceof SignInterface)) {
                cblock = world.getBlockEntity(
                        pt.getBlockX(), pt.getBlockY() - 1, pt.getBlockZ());
            }

            if (cblock == null || !(cblock instanceof SignInterface)) {
                return;
            }

            SignInterface sign = (SignInterface) cblock;
            String line2 = sign.getLine2();

            if (!line2.equalsIgnoreCase("[Station]")) {
                return;
            }

            Vector motion;
            int data = world.getData(
                    pt.getBlockX(), cblock.getPosition().getBlockY(), pt.getBlockZ());

            if (data == 0x0) {
                motion = new Vector(0, 0, -0.3);
            } else if (data == 0x4) {
                motion = new Vector(0.3, 0, 0);
            } else if (data == 0x8) {
                motion = new Vector(0, 0, 0.3);
            } else if (data == 0xC) {
                motion = new Vector(-0.3, 0, 0);
            } else {
                return;
            }

            for (MinecartInterface minecart : world.getMinecartList()) {
                int cartX = (int) Math.floor(minecart.getX());
                int cartY = (int) Math.floor(minecart.getY());
                int cartZ = (int) Math.floor(minecart.getZ());

                if (cartX == pt.getBlockX()
                        && cartY == pt.getBlockY() + 1
                        && cartZ == pt.getBlockZ()) {
                    minecart.setXSpeed(motion.getX());
                    minecart.setYSpeed(motion.getY());
                    minecart.setZSpeed(motion.getZ());
                }
            }
        }
    }

    /**
     * Called when a vehicle changes block
     *
     * @param vehicle the vehicle
     * @param blockX  coordinate x
     * @param blockY  coordinate y
     * @param blockZ  coordinate z
     */
    @Override
    public void onMinecartPositionChange(WorldInterface world, MinecartInterface minecart,
                                         int blockX, int blockY, int blockZ) {

        if (!minecartControlBlocks && !minecartTrackMessages && !minecartDispensers) {
            return;
        }

        Vector underPt = new Vector(blockX, blockY - 1, blockZ);
        int under = world.getId(blockX, blockY - 1, blockZ);

        if (minecartControlBlocks) {
            // Overflow prevention
            if (Math.abs(minecart.getXSpeed()) > 100) {
                minecart.setXSpeed(Math.signum(minecart.getXSpeed()) * 100);
            }

            if (Math.abs(minecart.getZSpeed()) > 100) {
                minecart.setZSpeed(Math.signum(minecart.getZSpeed()) * 100);
            }

            if (under == minecart25xBoostBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    minecart.setXSpeed(minecart.getXSpeed() * 1.25);
                    minecart.setZSpeed(minecart.getZSpeed() * 1.25);
                    return;
                }
            } else if (under == minecart100xBoostBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    minecart.setXSpeed(minecart.getXSpeed() * 2);
                    minecart.setZSpeed(minecart.getZSpeed() * 2);
                    return;
                }
            } else if (under == minecart50xSlowBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    minecart.setXSpeed(minecart.getXSpeed() * 0.5);
                    minecart.setZSpeed(minecart.getZSpeed() * 0.5);
                    return;
                }
            } else if (under == minecart20xSlowBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    minecart.setXSpeed(minecart.getXSpeed() * 0.8);
                    minecart.setZSpeed(minecart.getZSpeed() * 0.8);
                    return;
                }
            } else if (under == minecartReverseBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    Vector signPos = new Vector(blockX, blockY - 2, blockZ);
                    boolean reverseX = true;
                    boolean reverseZ = true;

                    // Directed reverse block
                    if (world.getId(signPos) == BlockType.SIGN_POST
                            && MinecraftUtil.doesSignSay(world, signPos, 1, "[Reverse]")) {
                        Vector dir = MinecraftUtil.getSignPostOrthogonalBack(world, signPos, 1)
                                .subtract(signPos);

                        // Acceptable sign direction
                        if (dir != null) {
                            if (CraftBookUtil.isSameSign(minecart.getXSpeed(),
                                    dir.getBlockX())) {
                                reverseX = false;
                            }
                            if (CraftBookUtil.isSameSign(minecart.getZSpeed(),
                                    dir.getBlockZ())) {
                                reverseZ = false;
                            }
                        }
                    }

                    if (reverseX) {
                        minecart.setXSpeed(minecart.getXSpeed() * -1);
                    }
                    if (reverseZ) {
                        minecart.setZSpeed(minecart.getZSpeed() * -1);
                    }

                    return;
                }
            } else if (under == minecartDepositBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    if (minecart.getType() == MinecartInterface.Type.STORAGE) {
                        Vector pt = new Vector(blockX, blockY, blockZ);
                        NearbyChestBlockBag bag = new NearbyChestBlockBag(pt);

                        for (int y = -1; y <= 0; y++) {
                            bag.addSingleSourcePosition(world, pt.add(1, y, 0));
                            bag.addSingleSourcePosition(world, pt.add(2, y, 0));
                            bag.addSingleSourcePosition(world, pt.add(-1, y, 0));
                            bag.addSingleSourcePosition(world, pt.add(-2, y, 0));
                            bag.addSingleSourcePosition(world, pt.add(0, y, 1));
                            bag.addSingleSourcePosition(world, pt.add(0, y, 2));
                            bag.addSingleSourcePosition(world, pt.add(0, y, -1));
                            bag.addSingleSourcePosition(world, pt.add(0, y, -2));
                        }

                        if (bag.getChestBlockCount() > 0) {
                            if (getControllerSign(world, pt.add(0, -1, 0), "[Deposit]") != null) {
                                InventoryUtil.moveChestBagToItemArray(
                                        (StorageMinecartInterface) minecart, bag);
                            } else {
                                InventoryUtil.moveItemArrayToChestBag(
                                        (StorageMinecartInterface) minecart, bag);
                            }
                        }
                    }
                }

                return;
            } else if (under == minecartEjectBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    PlayerInterface player = minecart.getPlayer();
                    if (player != null) {
                        // Let's find a place to put the player
                        Tuple3<Integer, Integer, Integer> loc =
                                new Tuple3<Integer, Integer, Integer>(blockX, blockY, blockZ);
                        Vector signPos = new Vector(blockX, blockY - 2, blockZ);

                        if (world.getId(signPos) == BlockType.SIGN_POST
                                && MinecraftUtil.doesSignSay(world, signPos, 1, "[Eject]")) {
                            Vector pos = MinecraftUtil.getSignPostOrthogonalBack(world, signPos, 1);

                            // Acceptable sign direction
                            if (pos != null) {
                                pos = pos.setY(blockY);

                                // Is the spot free?
                                if (BlockType.canPassThrough(world.getId(pos.add(0, 1, 0)))
                                        && BlockType.canPassThrough(world.getId(pos))) {
                                    loc = new Tuple3<Integer, Integer, Integer>(
                                            pos.getBlockX(),
                                            pos.getBlockY(),
                                            pos.getBlockZ());

                                    BlockEntity cBlock = world.getBlockEntity(
                                            blockX, blockY - 2, blockZ);

                                    if (cBlock instanceof SignInterface) {
                                        SignInterface sign = (SignInterface) cBlock;
                                        String text = sign.getLine1();
                                        if (text.length() > 0) {
                                            player.sendMessage(Colors.GOLD + "You've arrived at: "
                                                    + text);
                                        }
                                    }
                                }
                            }
                        }

                        player.setX(loc.a + 0.5);
                        player.setY(loc.b + 0.1);
                        player.setX(loc.c + 0.5);
                    }
                }

                return;
            } else if (under == minecartSortBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test == null || test) {
                    SignInterface sign =
                            getControllerSign(world, blockX, blockY - 1, blockZ, "[Sort]");

                    if (sign != null) {
                        SortDir dir = SortDir.FORWARD;

                        if (satisfiesCartSort(sign.getLine3(), minecart,
                                new Vector(blockX, blockY, blockZ))) {
                            dir = SortDir.LEFT;
                        } else if (satisfiesCartSort(sign.getLine4(), minecart,
                                new Vector(blockX, blockY, blockZ))) {
                            dir = SortDir.RIGHT;
                        }

                        int signData = world.getData(
                                sign.getX(), sign.getY(), sign.getZ());
                        int newData = 0;
                        Vector targetTrack = null;

                        if (signData == 0x8) { // West
                            if (dir == SortDir.LEFT) {
                                newData = 9;
                            } else if (dir == SortDir.RIGHT) {
                                newData = 8;
                            } else {
                                newData = 0;
                            }
                            targetTrack = new Vector(blockX, blockY, blockZ + 1);
                        } else if (signData == 0x0) { // East
                            if (dir == SortDir.LEFT) {
                                newData = 7;
                            } else if (dir == SortDir.RIGHT) {
                                newData = 6;
                            } else {
                                newData = 0;
                            }
                            targetTrack = new Vector(blockX, blockY, blockZ - 1);
                        } else if (signData == 0xC) { // North
                            if (dir == SortDir.LEFT) {
                                newData = 6;
                            } else if (dir == SortDir.RIGHT) {
                                newData = 9;
                            } else {
                                newData = 1;
                            }
                            targetTrack = new Vector(blockX - 1, blockY, blockZ);
                        } else if (signData == 0x4) { // South
                            if (dir == SortDir.LEFT) {
                                newData = 8;
                            } else if (dir == SortDir.RIGHT) {
                                newData = 7;
                            } else {
                                newData = 1;
                            }
                            targetTrack = new Vector(blockX + 1, blockY, blockZ);
                        }

                        if (targetTrack != null
                                && world.getId(targetTrack) == BlockType.MINECART_TRACKS) {
                            world.setData(targetTrack, newData);
                        }
                    }
                }

                return;
            }
        }

        if (minecartTrackMessages
                && world.getId(underPt.add(0, -1, 0)) == BlockType.SIGN_POST) {
            Vector signPos = underPt.add(0, -1, 0);

            Boolean test = RedstoneUtil.testAnyInput(world, signPos);

            if (test == null || test) {
                BlockEntity cblock = world.getBlockEntity(
                        signPos.getBlockX(), signPos.getBlockY(), signPos.getBlockZ());

                if (!(cblock instanceof SignInterface)) {
                    return;
                }

                SignInterface sign = (SignInterface) cblock;
                String line1 = sign.getLine1();

                if (line1.equalsIgnoreCase("[Print]")) {
                    if (!minecart.hasPlayer()) {
                        return;
                    }
                    PlayerInterface player = minecart.getPlayer();

                    String name = player.getName();
                    String msg = sign.getLine2() + sign.getLine3() + sign.getLine4();
                    long now = System.currentTimeMillis();

                    if (lastMinecartMsg.containsKey(name)) {
                        String lastMessage = lastMinecartMsg.get(name);
                        if (lastMessage.equals(msg)
                                && now < lastMinecartMsgTime.get(name) + 3000) {
                            return;
                        }
                    }

                    lastMinecartMsg.put(name, msg);
                    lastMinecartMsgTime.put(name, now);
                    player.sendMessage("> " + msg);
                }
            }
        }

        if (minecartDispensers) {
            Vector pt = new Vector(blockX, blockY, blockZ);
            Vector depositPt = null;

            if (world.getId(pt.add(1, 0, 0)) == BlockType.CHEST
                    && (world.getId(pt.add(-1, 0, 0)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(-1, -1, 0)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(-1, 1, 0)) == BlockType.MINECART_TRACKS)) {
                depositPt = pt.add(1, 0, 0);
            } else if (world.getId(pt.add(-1, 0, 0)) == BlockType.CHEST
                    && (world.getId(pt.add(1, 0, 0)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(1, -1, 0)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(1, 1, 0)) == BlockType.MINECART_TRACKS)) {
                depositPt = pt.add(-1, 0, 0);
            } else if (world.getId(pt.add(0, 0, 1)) == BlockType.CHEST
                    && (world.getId(pt.add(0, 0, -1)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(0, -1, -1)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(0, 1, -1)) == BlockType.MINECART_TRACKS)) {
                depositPt = pt.add(0, 0, 1);
            } else if (world.getId(pt.add(0, 0, -1)) == BlockType.CHEST
                    && (world.getId(pt.add(0, 0, 1)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(0, -1, 1)) == BlockType.MINECART_TRACKS
                    || world.getId(pt.add(0, 1, 1)) == BlockType.MINECART_TRACKS)) {
                depositPt = pt.add(0, 0, -1);
            }

            if (depositPt != null) {
                SignInterface sign =
                        getControllerSign(world, depositPt.add(0, -1, 0), "[Dispenser]");
                String collectType = sign != null ? sign.getLine2() : "";

                NearbyChestBlockBag blockBag = new NearbyChestBlockBag(depositPt);
                blockBag.addSingleSourcePosition(world, depositPt);
                blockBag.addSingleSourcePosition(world, depositPt.add(1, 0, 0));
                blockBag.addSingleSourcePosition(world, depositPt.add(-1, 0, 0));
                blockBag.addSingleSourcePosition(world, depositPt.add(0, 0, 1));
                blockBag.addSingleSourcePosition(world, depositPt.add(0, 0, -1));

                MinecartInterface.Type type = minecart.getType();

                if (type == MinecartInterface.Type.REGULAR) {
                    try {
                        blockBag.storeBlock(ItemType.MINECART);
                        minecart.remove();
                    } catch (BlockBagException e) {
                    }
                } else if (type == MinecartInterface.Type.STORAGE) {
                    try {
                        InventoryUtil.moveItemArrayToChestBag(
                                (StorageMinecartInterface) minecart, blockBag);

                        if (collectType.equalsIgnoreCase("Storage")) {
                            blockBag.storeBlock(ItemType.STORAGE_MINECART);
                        } else {
                            blockBag.storeBlock(ItemType.MINECART);
                            blockBag.storeBlock(BlockType.CHEST);
                        }

                        minecart.remove();
                    } catch (BlockBagException e) {
                        // Ran out of space
                    }
                } else if (type == MinecartInterface.Type.POWERED) {
                    try {
                        if (collectType.equalsIgnoreCase("Powered")) {
                            blockBag.storeBlock(ItemType.POWERED_MINECART);
                        } else {
                            blockBag.storeBlock(ItemType.MINECART);
                            blockBag.storeBlock(BlockType.FURNACE);
                        }
                        minecart.remove();
                    } catch (BlockBagException e) {
                        // Ran out of space
                    }
                }

                blockBag.flushChanges();
            }
        }
    }

    /**
     * Called when a vehicle enters or leaves a block
     *
     * @param vehicle the vehicle
     */
    @Override
    public void onMinecartVelocityChange(WorldInterface world, MinecartInterface minecart) {

        if (!minecartControlBlocks && !unoccupiedCoast) {
            return;
        }

        int blockX = (int) Math.floor(minecart.getX());
        int blockY = (int) Math.floor(minecart.getY());
        int blockZ = (int) Math.floor(minecart.getZ());
        Vector underPt = new Vector(blockX, blockY - 1, blockZ);
        int under = world.getId(blockX, blockY - 1, blockZ);

        if (minecartControlBlocks) {
            if (under == minecartStationBlock) {
                Boolean test = RedstoneUtil.testAnyInput(world, underPt);

                if (test != null) {
                    if (!test) {
                        minecart.setXSpeed(0);
                        minecart.setYSpeed(0);
                        minecart.setZSpeed(0);
                        return;
                    } else {
                        BlockEntity cblock = world.getBlockEntity(
                                blockX, blockY - 2, blockZ);

                        // Maybe it's the sign directly below
                        if (cblock == null || !(cblock instanceof SignInterface)) {
                            cblock = world.getBlockEntity(
                                    blockX, blockY - 3, blockZ);
                        }

                        if (cblock != null && cblock instanceof SignInterface) {
                            SignInterface sign = (SignInterface) cblock;
                            String line2 = sign.getLine2();

                            if (!line2.equalsIgnoreCase("[Station]")) {
                                return;
                            }

                            String line3 = sign.getLine3();
                            String line4 = sign.getLine4();

                            if (line3.equalsIgnoreCase("Pulse")
                                    || line4.equalsIgnoreCase("Pulse")) {
                                return;
                            }

                            Vector motion = null;
                            int data = world.getData(
                                    blockX, cblock.getPosition().getBlockY(), blockZ);

                            if (data == 0x0) {
                                motion = new Vector(0, 0, -0.3);
                            } else if (data == 0x4) {
                                motion = new Vector(0.3, 0, 0);
                            } else if (data == 0x8) {
                                motion = new Vector(0, 0, 0.3);
                            } else if (data == 0xC) {
                                motion = new Vector(-0.3, 0, 0);
                            }

                            if (motion != null) {
                                if (!CraftBookUtil.isSameSign(minecart.getXSpeed(), motion.getX())
                                        || minecart.getXSpeed() < motion.getX()) {
                                    minecart.setXSpeed(motion.getX());
                                }
                                if (!CraftBookUtil.isSameSign(minecart.getYSpeed(), motion.getY())
                                        || minecart.getYSpeed() < motion.getY()) {
                                    minecart.setYSpeed(motion.getY());
                                }
                                if (!CraftBookUtil.isSameSign(minecart.getZSpeed(), motion.getZ())
                                        || minecart.getZSpeed() < motion.getZ()) {
                                    minecart.setZSpeed(motion.getZ());
                                }

                                return;
                            }
                        }
                    }
                }
            }
        }

        int block = world.getId(blockX, blockY, blockZ);
        if (slickPressurePlates
                && (block == BlockType.STONE_PRESSURE_PLATE
                || block == BlockType.WOODEN_PRESSURE_PLATE)) {
            // Numbers from code
            minecart.setXSpeed(minecart.getXSpeed() / 0.55);
            minecart.setYSpeed(0);
            minecart.setZSpeed(minecart.getZSpeed() / 0.55);
        }

        if (unoccupiedCoast && minecart.getPlayer() == null) {
            minecart.setXSpeed(minecart.getXSpeed() * 1.018825);
            minecart.setZSpeed(minecart.getZSpeed() * 1.018825);
        }
    }

    /**
     * Called when vehicle receives damage
     *
     * @param vehicle
     * @param attacker entity that dealt the damage
     * @param damage
     *
     * @return false to set damage
     */
    @Override
    public boolean onMinecartDamage(WorldInterface world, MinecartInterface cart,
                                    BaseEntityInterface attacker, int damage) {

        if (!inCartControl) {
            return false;
        }

        PlayerInterface passenger = cart.getPlayer();

        // Player.equals() now works correctly as of recent hMod versions
        if (passenger != null) {
            double speed2 = cart.getXSpeed() * cart.getXSpeed() +
                    cart.getYSpeed() * cart.getYSpeed() +
                    cart.getZSpeed() * cart.getZSpeed();

            if (speed2 > 0.01 * 0.01) { // Stop the cart
                cart.setXSpeed(0);
                cart.setYSpeed(0);
                cart.setZSpeed(0);
            } else {
                // From hey0's code, and then stolen from WorldEdit
                double rot = (passenger.getYaw() - 90) % 360;
                if (rot < 0) {
                    rot += 360.0;
                }

                if (0 <= rot && rot < 22.5) {
                    cart.setXSpeed(-0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(0);
                } else if (22.5 <= rot && rot < 67.5) {
                    cart.setXSpeed(-0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(-0.1);
                } else if (67.5 <= rot && rot < 112.5) {
                    cart.setXSpeed(0);
                    cart.setYSpeed(0);
                    cart.setZSpeed(-0.1);
                } else if (112.5 <= rot && rot < 157.5) {
                    cart.setXSpeed(0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(-0.1);
                } else if (157.5 <= rot && rot < 202.5) {
                    cart.setXSpeed(0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(0);
                } else if (202.5 <= rot && rot < 247.5) {
                    cart.setXSpeed(0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(0.1);
                } else if (247.5 <= rot && rot < 292.5) {
                    cart.setXSpeed(0);
                    cart.setYSpeed(0);
                    cart.setZSpeed(0.1);
                } else if (292.5 <= rot && rot < 337.5) {
                    cart.setXSpeed(-0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(0.1);
                } else if (337.5 <= rot && rot < 360.0) {
                    cart.setXSpeed(-0.1);
                    cart.setYSpeed(0);
                    cart.setZSpeed(0);
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Called when someone places a block. Return true to prevent the placement.
     *
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     *
     * @return true if you want to undo the block placement
     */
    public boolean onBlockPlace(WorldInterface world, PlayerInterface player,
                                Vector blockPlaced, Vector blockClicked, int itemInHand) {

        if (world.getId(blockPlaced) == BlockType.MINECART_TRACKS) {
            int under = world.getId(blockPlaced.getBlockX(),
                    blockPlaced.getBlockY() - 1, blockPlaced.getBlockZ());

            if (minecartControlBlocks && under == minecartStationBlock) {
                SignInterface sign = getControllerSign(world, blockPlaced.getBlockX(),
                        blockPlaced.getBlockY() - 1, blockPlaced.getBlockZ(), "[Station]");
                Vector pt = new Vector(blockPlaced.getX(),
                        blockPlaced.getBlockY() - 1, blockPlaced.getBlockZ());

                boolean needsRedstone = RedstoneUtil.testAnyInput(world, pt) == null;

                if (sign == null && needsRedstone) {
                    player.sendMessage(Colors.GOLD
                            + "Two things to do: Wire the block and place a [Station] sign.");
                } else if (sign == null) {
                    player.sendMessage(Colors.RED
                            + "Place a [Station] sign 1-2 blocks underneath.");
                } else if (needsRedstone) {
                    player.sendMessage(Colors.RED
                            + "To make the station work, wire it up with redstone.");
                } else {
                    player.sendMessage(Colors.RED
                            + "Minecart station created.");
                }
            } else if (minecartControlBlocks && under == minecart25xBoostBlock) {
                player.sendMessage(Colors.GOLD + "Minecart boost block created.");
            } else if (minecartControlBlocks && under == minecart100xBoostBlock) {
                player.sendMessage(Colors.GOLD + "Minecart boost block created.");
            } else if (minecartControlBlocks && under == minecart50xSlowBlock) {
                player.sendMessage(Colors.GOLD + "Minecart brake block created.");
            } else if (minecartControlBlocks && under == minecart20xSlowBlock) {
                player.sendMessage(Colors.GOLD + "Minecart brake block created.");
            } else if (minecartControlBlocks && under == minecartReverseBlock) {
                player.sendMessage(Colors.GOLD + "Minecart reverse block created.");
            } else if (minecartControlBlocks && under == minecartSortBlock) {
                SignInterface sign = getControllerSign(world, blockPlaced.getBlockX(),
                        blockPlaced.getBlockY() - 1, blockPlaced.getBlockZ(), "[Sort]");
                //Vector pt = new Vector(blockPlaced.getX(),
                //    blockPlaced.getY() - 1, blockPlaced.getZ());

                if (sign == null) {
                    player.sendMessage(Colors.RED
                            + "A [Sort] sign is still needed.");
                } else {
                    player.sendMessage(Colors.GOLD
                            + "Minecart sort block created.");
                }
            }
        }

        return false;
    }

    /**
     * Called when a sign is updated.
     *
     * @param player
     * @param cblock
     *
     * @return
     */
    public boolean onSignChange(PlayerInterface player, WorldInterface world, Vector v, SignInterface s) {

        int type = world.getId(
                s.getX(), s.getY(), s.getZ());

        String line1 = s.getLine1();
        String line2 = s.getLine2();

        // Station
        if (line2.equalsIgnoreCase("[Station]")) {
            craftBook.informUser(player);

            s.setLine2("[Station]");
            s.flushChanges();

            if (minecartControlBlocks) {
                int data = world.getData(
                        s.getX(), s.getY(), s.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.RED + "The sign must be a sign post.");
                    MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.RED + "The sign cannot be at an odd angle.");
                    MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                    return true;
                }

                player.sendMessage(Colors.GOLD + "Station sign detected.");
            } else {
                player.sendMessage(Colors.RED
                        + "Minecart control blocks are disabled on this server.");
            }
            // Sort
        } else if (line2.equalsIgnoreCase("[Sort]")) {
            craftBook.informUser(player);

            s.setLine2("[Sort]");
            s.flushChanges();

            if (minecartControlBlocks) {
                int data = world.getData(
                        s.getX(), s.getY(), s.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.RED + "The sign must be a sign post.");
                    MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.RED + "The sign cannot be at an odd angle.");
                    MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                    return true;
                }

                player.sendMessage(Colors.GOLD + "Sort sign detected.");
            } else {
                player.sendMessage(Colors.RED
                        + "Minecart control blocks are disabled on this server.");
            }
            // Dispenser
        } else if (line2.equalsIgnoreCase("[Dispenser]")) {
            craftBook.informUser(player);

            s.setLine2("[Dispenser]");
            s.flushChanges();

            player.sendMessage(Colors.GOLD + "Dispenser sign detected.");
            // Print
        } else if (line1.equalsIgnoreCase("[Print]")) {
            craftBook.informUser(player);

            s.setLine1("[Print]");
            s.flushChanges();

            player.sendMessage(Colors.GOLD + "Message print block detected.");
        }

        return false;
    }

    /**
     * Called when a player enter or leaves a vehicle
     *
     * @param vehicle the vehicle
     * @param player  the player
     */
    public void onMinecartEnter(WorldInterface world, MinecartInterface cart,
                                BaseEntityInterface entity, boolean entering) {

        if (decayWatcher != null) {
            decayWatcher.trackEnter(world, cart);
        }

        if (minecartDestroyOnExit && entering) {
            cart.remove();

            if (minecartDropOnExit && cart.getPlayer() != null) {
                cart.getPlayer().giveItem(ItemType.MINECART, 1);
            }
        }
    }

    /**
     * Called when a vehicle is destroyed
     *
     * @param vehicle the vehicle
     */
    public void onMinecartDestroyed(WorldInterface world, MinecartInterface cart) {

        if (decayWatcher != null) {
            decayWatcher.forgetMinecart(cart);
        }
    }

    public void onWorldUnload(WorldInterface world) {

        decayWatcher.removeWorld(world);
    }

    /**
     * Called on plugin unload.
     */
    public void disable() {

        if (decayWatcher != null) {
            decayWatcher.disable();
        }
    }

    /**
     * Get the controller sign for a block type. The coordinates provided
     * are those of the block (signs are to be underneath). The provided
     * text must be on the second line of the sign.
     *
     * @param pt
     * @param text
     *
     * @return
     */
    private SignInterface getControllerSign(WorldInterface w, Vector pt, String text) {

        return getControllerSign(w, pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ(), text);
    }

    /**
     * Get the controller sign for a block type. The coordinates provided
     * are those of the block (signs are to be underneath). The provided
     * text must be on the second line of the sign.
     *
     * @param x
     * @param y
     * @param z
     * @param text
     *
     * @return
     */
    private SignInterface getControllerSign(WorldInterface w, int x, int y, int z, String text) {

        BlockEntity cblock = w.getBlockEntity(x, y - 1, z);

        if (cblock instanceof SignInterface
                && ((SignInterface) cblock).getLine2().equalsIgnoreCase(text)) {
            return (SignInterface) cblock;
        }

        cblock = w.getBlockEntity(x, y - 2, z);

        if (cblock instanceof SignInterface
                && ((SignInterface) cblock).getLine2().equalsIgnoreCase(text)) {
            return (SignInterface) cblock;
        }

        return null;
    }

    /**
     * Returns true if a filter line satisfies the conditions.
     *
     * @param line
     * @param minecart
     * @param trackPos
     *
     * @return
     */
    public boolean satisfiesCartSort(String line, MinecartInterface minecart, Vector trackPos) {

        PlayerInterface player = minecart.getPlayer();

        if (line.equalsIgnoreCase("All")) {
            return true;
        }

        if ((line.equalsIgnoreCase("Unoccupied")
                || line.equalsIgnoreCase("Empty"))
                && !minecart.hasPassenger()) {
            return true;
        }

        if (line.equalsIgnoreCase("Storage")
                && minecart.getType() == MinecartInterface.Type.STORAGE) {
            return true;
        }

        if (line.equalsIgnoreCase("Powered")
                && minecart.getType() == MinecartInterface.Type.POWERED) {
            return true;
        }

        if (line.equalsIgnoreCase("Minecart")
                && minecart.getType() == MinecartInterface.Type.REGULAR) {
            return true;
        }

        if ((line.equalsIgnoreCase("Occupied")
                || line.equalsIgnoreCase("Full"))
                && minecart.hasPassenger()) {
            return true;
        }

        if (line.equalsIgnoreCase("Animal")
                && minecart.hasAnimal()) {
            return true;
        }

        if (line.equalsIgnoreCase("Mob")
                && minecart.hasMob()) {
            return true;
        }

        if ((line.equalsIgnoreCase("Player")
                || line.equalsIgnoreCase("Ply"))
                && minecart.hasPlayer()) {
            return true;
        }

        if (minecart.hasPlayer()) {
            String stop = stopStation.get(player.getName());
            if (stop != null && stop.equals(line)) {
                return true;
            }
        }

        String[] parts = line.split(":");

        if (parts.length >= 2) {
            if (minecart.hasPlayer() && parts[0].equalsIgnoreCase("Held")) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    if (player.getItemInHand() == item) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                }
            } else if (minecart.hasPlayer() && parts[0].equalsIgnoreCase("Group")) {
                if (player.isInGroup(parts[1])) {
                    return true;
                }
            } else if (minecart.hasPlayer() && parts[0].equalsIgnoreCase("Ply")) {
                if (parts[1].equalsIgnoreCase(player.getName())) {
                    return true;
                }
            } else if (parts[0].equalsIgnoreCase("Mob")) {
                String testMob = parts[1];
                minecart.isMobType(testMob);
            }
        }

        return false;
    }

    /**
     * @param player
     */
    @Override
    public void onDisconnect(PlayerInterface player) {

        lastMinecartMsg.remove(player.getName());
        lastMinecartMsgTime.remove(player.getName());
        stopStation.remove(player.getName());
    }
}
