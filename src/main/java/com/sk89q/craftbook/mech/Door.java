package com.sk89q.craftbook.mech;

// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InvalidConstructionException;
import com.sk89q.craftbook.util.exceptions.InvalidDirectionException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.craftbook.util.exceptions.UnacceptableMaterialException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * Door.
 *
 * @author turtle9598
 */
public class Door extends AbstractMechanic {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    public static class Factory extends AbstractMechanicFactory<Door> {

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public Door detect(BlockWorldVector pt, LocalPlayer player,
                ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Door Down]")) {
                player.checkPermission("craftbook.mech.door");

                sign.setLine(1, "[Door Down]");
                if (sign.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.door" + "" +
                        ".infinite")) {
                    sign.setLine(0, "0");
                } else if (!sign.getLine(0).equalsIgnoreCase("infinite")) {
                    sign.setLine(0, "0");
                }
                sign.update(false);
                player.print("mech.door.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[Door Up]")) {
                player.checkPermission("craftbook.mech.door");

                sign.setLine(1, "[Door Up]");
                if (sign.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.door" + "" +
                        ".infinite")) {
                    sign.setLine(0, "0");
                } else if (!sign.getLine(0).equalsIgnoreCase("infinite")) {
                    sign.setLine(0, "0");
                }
                sign.update(false);
                player.print("mech.door.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[Door]")) {
                player.checkPermission("craftbook.mech.door");

                sign.setLine(1, "[Door]");
                if (sign.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.door" + "" +
                        ".infinite")) {
                    sign.setLine(0, "0");
                } else if (!sign.getLine(0).equalsIgnoreCase("infinite")) {
                    sign.setLine(0, "0");
                }
                sign.update(false);
                player.print("mech.door.create");
            } else return null;

            throw new ProcessedMechanismException();
        }

        /**
         * Explore around the trigger to find a Door; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return a Door if we could make a valid one, or null if this looked nothing like a door.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be a door, but it failed.
         */
        @Override
        public Door detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            if (block.getTypeId() != BlockID.SIGN_POST) return null;
            ChangedSign s = BukkitUtil.toChangedSign(block);
            if (!s.getLine(1).contains("Door") || s.getLine(1).equalsIgnoreCase("[Door]"))
                return null;

            // okay, now we can start doing exploration of surrounding blocks
            // and if something goes wrong in here then we throw fits.
            return new Door(block);
        }
    }

    /**
     * @param trigger if you didn't already check if this is a signpost with appropriate text,
     *                you're going on Santa's naughty list.
     *
     * @throws InvalidMechanismException
     */
    private Door(Block trigger) throws InvalidMechanismException {

        super();

        // check and set some properties
        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();

        this.trigger = trigger;

        if (trigger == null) return;

        ChangedSign s = BukkitUtil.toChangedSign(trigger);

        int block;
        findBase:
        {
            if (s.getLine(1).equalsIgnoreCase("[Door Up]")) {
                proximalBaseCenter = trigger.getRelative(BlockFace.UP);
            } else if (s.getLine(1).equalsIgnoreCase("[Door Down]")) {
                proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
            } else throw new InvalidConstructionException("Sign is incorrectly made.");

            block = proximalBaseCenter.getTypeId();

            if (plugin.getConfiguration().doorBlocks.contains(block))
                break findBase;
            else throw new UnacceptableMaterialException("mech.door.unusable");
        }
        // Find the other side
        if (s.getLine(1).equalsIgnoreCase("[Door Up]")) {
            otherSide = trigger.getRelative(BlockFace.UP);
        } else if (s.getLine(1).equalsIgnoreCase("[Door Down]")) {
            otherSide = trigger.getRelative(BlockFace.DOWN);
        }
        for (int i = 0; i <= plugin.getConfiguration().doorMaxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (otherSide.getTypeId() == BlockID.SIGN_POST) {
                String otherSignText = BukkitUtil.toChangedSign(otherSide).getLines()[1];
                if ("[Door Down]".equalsIgnoreCase(otherSignText))
                    break;
                if ("[Door Up]".equalsIgnoreCase(otherSignText))
                    break;
                if ("[Door]".equalsIgnoreCase(otherSignText))
                    break;
            }

            if (s.getLine(1).equalsIgnoreCase("[Door Up]")) {
                otherSide = otherSide.getRelative(BlockFace.UP);
            } else if (s.getLine(1).equalsIgnoreCase("[Door Down]")) {
                otherSide = otherSide.getRelative(BlockFace.DOWN);
            }
        }

        if (otherSide.getTypeId() != BlockID.SIGN_POST) throw new InvalidConstructionException("mech.door.other-sign");
        // Check the other side's base blocks for matching type
        Block distalBaseCenter = null;

        if (s.getLine(1).equalsIgnoreCase("[Door Up]")) {
            distalBaseCenter = otherSide.getRelative(BlockFace.DOWN);
        } else if (s.getLine(1).equalsIgnoreCase("[Door Down]")) {
            distalBaseCenter = otherSide.getRelative(BlockFace.UP);
        }

        if (distalBaseCenter == null || distalBaseCenter.getTypeId() != block && distalBaseCenter.getData() != proximalBaseCenter.getData())
            throw new InvalidConstructionException("mech.door.material");

        // Select the togglable region
        toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter), BukkitUtil.toVector(distalBaseCenter));
        int left, right;
        try {
            left = Math.max(0, Math.min(plugin.getConfiguration().doorMaxWidth, Integer.parseInt(s.getLine(2))));
        } catch (Exception e) {
            left = 1;
        }
        try {
            right = Math.max(0, Math.min(plugin.getConfiguration().doorMaxWidth, Integer.parseInt(s.getLine(3))));
        } catch (Exception e) {
            right = 1;
        }

        // Expand Left
        for (int i = 0; i < left; i++) {
            if(distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getTypeId() != proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getTypeId() && distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getData() != proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getData())
                throw new InvalidConstructionException("mech.door.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)), new Vector(0, 0, 0));
        }

        // Expand Right
        for (int i = 0; i < right; i++) {
            if(distalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getTypeId() != proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getTypeId() && distalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getData() != proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getData())
                throw new InvalidConstructionException("mech.door.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)), new Vector(0, 0, 0));
        }

        // Don't toggle the end points
        toggle.contract(BukkitUtil.toVector(BlockFace.UP), BukkitUtil.toVector(BlockFace.DOWN));
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getConfiguration().doorEnabled) return;

        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger))) return;

        LocalPlayer player = new BukkitPlayer(plugin, event.getPlayer());
        if (!player.hasPermission("craftbook.mech.door.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if (plugin.getConfiguration().safeDestruction && event.getPlayer().getItemInHand() != null)
            if (getDoorMaterial() == event.getPlayer().getItemInHand().getTypeId()) {

                if (!player.hasPermission("craftbook.mech.door.restock")) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        player.printError("mech.restock-permission");
                    return;
                }

                ChangedSign sign = null;

                if (SignUtil.isSign(event.getClickedBlock()))
                    sign = BukkitUtil.toChangedSign(event.getClickedBlock());

                if (sign != null) {
                    int amount = 1;
                    if (event.getPlayer().isSneaking() && event.getPlayer().getItemInHand().getAmount() >= 5)
                        amount = 5;
                    addBlocks(sign, amount);

                    if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE))
                        if (event.getPlayer().getItemInHand().getAmount() <= amount) {
                            event.getPlayer().setItemInHand(new ItemStack(0, 0));
                        } else {
                            event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - amount);
                        }

                    player.print("mech.restock");
                    event.setCancelled(true);
                    return;
                }
            }

        event.setCancelled(true);

        if(flipState(player))
            player.print("mech.door.toggle");
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (!plugin.getConfiguration().doorAllowRedstone) return;

        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return;
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                flipState(null);
            }
        }, 2L);
    }

    private boolean flipState(LocalPlayer player) {
        // this is kinda funky, but we only check one position
        // to see if the door is open and/or closable.
        // efficiency choice :/
        Block hinge;

        if (BukkitUtil.toChangedSign(trigger).getLine(1).equalsIgnoreCase("[Door Up]")) {
            hinge = proximalBaseCenter.getRelative(BlockFace.UP);
        } else {
            hinge = proximalBaseCenter.getRelative(BlockFace.DOWN);
        }

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden door, just weird
        // results.
        if (BlockUtil.isBlockReplacable(hinge.getTypeId()) && getDoorMaterial() != hinge.getTypeId()) {
            return closeDoor(player);
        } else {
            return openDoor();
        }
    }

    public boolean openDoor() {

        ChangedSign s = BukkitUtil.toChangedSign(trigger);
        for (Vector bv : toggle) {
            Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            int oldType = b.getTypeId();
            if (b.getTypeId() == getDoorMaterial() || BlockUtil.isBlockReplacable(b.getTypeId())) {
                b.setTypeId(BlockID.AIR);
                if (plugin.getConfiguration().safeDestruction) {
                    if (oldType != 0) {
                        addBlocks(s, 1);
                    }
                }
            }
        }

        return true;
    }

    public boolean closeDoor(LocalPlayer player) {

        ChangedSign s = BukkitUtil.toChangedSign(trigger);
        for (Vector bv : toggle) {
            Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (BlockUtil.isBlockReplacable(b.getTypeId())) {
                if (plugin.getConfiguration().safeDestruction) {
                    if (hasEnoughBlocks(s)) {
                        b.setTypeId(getDoorMaterial());
                        b.setData(getDoorData());
                        removeBlocks(s, 1);
                    } else {
                        if (player != null)
                            player.printError("mech.not-enough-blocks");
                        return false;
                    }
                } else {
                    b.setTypeId(getDoorMaterial());
                    b.setData(getDoorData());
                }
            }
        }

        return true;
    }

    private int getDoorMaterial() {

        return proximalBaseCenter.getTypeId();
    }

    private byte getDoorData() {

        return proximalBaseCenter.getData();
    }

    /**
     * The signpost we came from.
     */
    private Block trigger;
    /**
     * The block that determines door type.
     */
    private Block proximalBaseCenter;
    /**
     * The signpost on the other end.
     */
    private Block otherSide;
    /**
     * The rectangle that we toggle.
     */
    private CuboidRegion toggle;

    // we don't store anything about the blocks on the ends because
    // we never poke them; just check that they're sane when we're building
    // the door. if this were a PersistentMechanic, those six blocks
    // would be considered defining blocks, though.

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        ChangedSign sign = null;

        if (SignUtil.isSign(event.getBlock()))
            sign = BukkitUtil.toChangedSign(event.getBlock());

        if (hasEnoughBlocks(sign)) {
            ItemStack toDrop = new ItemStack(getDoorMaterial(), getBlocks(sign), getDoorData());
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
        }
    }

    public boolean removeBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) - amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public boolean addBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) + amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public void setBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return;
        s.setLine(0, String.valueOf(amount));
        s.update(false);
    }

    public int getBlocks(ChangedSign s) {

        ChangedSign otherSign = BukkitUtil.toChangedSign(otherSide);
        return getBlocks(s, otherSign);
    }

    public int getBlocks(ChangedSign s, ChangedSign other) {

        if (s.getLine(0).equalsIgnoreCase("infinite") || other != null && other.getLine(0).equalsIgnoreCase("infinite"))
            return 0;
        int curBlocks = 0;
        try {
            curBlocks = Integer.parseInt(s.getLine(0));
            if(other != null) {
                try {
                    curBlocks += Integer.parseInt(other.getLine(0));
                    setBlocks(s, curBlocks);
                    setBlocks(other, 0);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            curBlocks = 0;
        }
        return curBlocks;
    }

    public boolean hasEnoughBlocks(ChangedSign s) {

        return !(s == null || s.getLine(0) == null) && (s.getLine(0).equalsIgnoreCase("infinite") || getBlocks(s) > 0);
    }
}