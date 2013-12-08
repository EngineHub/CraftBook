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

package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InvalidConstructionException;
import com.sk89q.craftbook.util.exceptions.InvalidDirectionException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.UnacceptableMaterialException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * The default bridge mechanism -- signposts on either side of a 3xN plane of (or 1xN plane if 1 on second line) blocks.
 *
 * @author hash
 */
public class Bridge extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[bridge]") && !event.getLine(1).equalsIgnoreCase("[bridge end]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.mech.bridge")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if (event.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.bridge.infinite"))
            event.setLine(0, "0");
        else if (!event.getLine(0).equalsIgnoreCase("infinite"))
            event.setLine(0, "0");

        if(event.getLine(1).equalsIgnoreCase("[bridge]")) {
            event.setLine(1, "[Bridge]");
            player.print("mech.bridge.create");
        } else if(event.getLine(1).equalsIgnoreCase("[bridge end]")) {
            event.setLine(1, "[Bridge End]");
            player.print("mech.bridge.end-create");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!SignUtil.isSign(event.getClickedBlock())) return;
        if (!BukkitUtil.toChangedSign(event.getClickedBlock()).getLine(1).equals("[Bridge]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.bridge.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        try {
            ChangedSign sign = null;

            if (SignUtil.isSign(event.getClickedBlock()))
                sign = BukkitUtil.toChangedSign(event.getClickedBlock());

            if (CraftBookPlugin.inst().getConfiguration().safeDestruction && sign != null && !sign.getLine(0).equalsIgnoreCase("infinite"))
                if (event.getPlayer().getItemInHand() != null)
                    if (getBridgeBase(event.getClickedBlock()).getType() == event.getPlayer().getItemInHand().getType()) {

                        if (!player.hasPermission("craftbook.mech.bridge.restock")) {
                            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                                player.printError("mech.restock-permission");
                            return;
                        }

                        int amount = 1;
                        if (event.getPlayer().isSneaking() && event.getPlayer().getItemInHand().getAmount() >= 5) {
                            amount = 5;
                        }
                        addBlocks(sign, BukkitUtil.toChangedSign(getFarSign(event.getClickedBlock())), amount);

                        if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE))
                            if (event.getPlayer().getItemInHand().getAmount() <= amount)
                                event.getPlayer().setItemInHand(new ItemStack(Material.AIR, 0));
                            else
                                event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - amount);

                        player.print("mech.restock");
                        event.setCancelled(true);
                        return;
                    }

            event.setCancelled(true);

            if(flipState(event.getClickedBlock(), player))
                player.print("mech.bridge.toggle");
        } catch (InvalidMechanismException e) {
            if(e.getMessage() != null)
                player.printError(e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().bridgeAllowRedstone) return;
        if (event.isMinor()) return;

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!BukkitUtil.toChangedSign(event.getBlock()).getLine(1).equals("[Bridge]")) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                try {
                    flipState(event.getBlock(), null);
                } catch (InvalidMechanismException e) {
                }
            }
        }, 2L);
    }

    public Block getBridgeBase(Block trigger) throws UnacceptableMaterialException {
        Block proximalBaseCenter = trigger.getRelative(BlockFace.UP);
        if (trigger.getY() < trigger.getWorld().getMaxHeight()-1 && CraftBookPlugin.inst().getConfiguration().bridgeBlocks.contains(new ItemInfo(proximalBaseCenter)))
            return proximalBaseCenter; // On Top

        // If we've reached this point nothing was found on the top, check the bottom
        proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
        if (trigger.getY() > 0 && CraftBookPlugin.inst().getConfiguration().bridgeBlocks.contains(new ItemInfo(proximalBaseCenter)))
            return proximalBaseCenter; // it's below

        proximalBaseCenter = trigger.getRelative(SignUtil.getBack(trigger));
        if (CraftBookPlugin.inst().getConfiguration().bridgeBlocks.contains(new ItemInfo(proximalBaseCenter)))
            return proximalBaseCenter; // it's behind
        else throw new UnacceptableMaterialException("mech.bridge.unusable");
    }

    public Block getFarSign(Block trigger) {

        BlockFace dir = SignUtil.getFacing(trigger);
        Block farSide = trigger.getRelative(dir);
        for (int i = 0; i <= CraftBookPlugin.inst().getConfiguration().bridgeMaxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (farSide.getType() == trigger.getType()) {
                String otherSignText = BukkitUtil.toChangedSign(farSide).getLine(1);
                if ("[Bridge]".equalsIgnoreCase(otherSignText) || "[Bridge End]".equalsIgnoreCase(otherSignText)) {
                    break;
                }
            }

            farSide = farSide.getRelative(dir);
        }

        return farSide;
    }

    private boolean flipState(Block trigger, LocalPlayer player) throws InvalidMechanismException {

        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        Block proximalBaseCenter = getBridgeBase(trigger);

        // Find the other side
        Block farSide = getFarSign(trigger);

        if (farSide.getType() != trigger.getType()) throw new InvalidConstructionException("mech.bridge.other-sign");

        // Check the other side's base blocks for matching type
        BlockFace face = trigger.getFace(proximalBaseCenter);
        if(face != BlockFace.UP && face != BlockFace.DOWN) face = face.getOppositeFace();
        Block distalBaseCenter = farSide.getRelative(face);
        if (!BlockUtil.areBlocksIdentical(distalBaseCenter, proximalBaseCenter))
            throw new InvalidConstructionException("mech.bridge.material");

        // Select the togglable region
        CuboidRegion toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter), BukkitUtil.toVector(distalBaseCenter));
        int left, right;
        try {
            left = Math.max(0, Math.min(CraftBookPlugin.inst().getConfiguration().bridgeMaxWidth, Integer.parseInt(BukkitUtil.toChangedSign(trigger).getLine(2))));
        } catch (Exception e) {
            left = 1;
        }
        try {
            right = Math.max(0, Math.min(CraftBookPlugin.inst().getConfiguration().bridgeMaxWidth, Integer.parseInt(BukkitUtil.toChangedSign(trigger).getLine(3))));
        } catch (Exception e) {
            right = 1;
        }

        // Expand Left
        for (int i = 0; i < left; i++) {
            if(!BlockUtil.areBlocksIdentical(distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i), proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i)))
                throw new InvalidConstructionException("mech.bridge.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)), new Vector(0, 0, 0));
        }

        // Expand Right
        for (int i = 0; i < right; i++) {
            if(!BlockUtil.areBlocksIdentical(distalBaseCenter.getRelative(SignUtil.getRight(trigger), i), proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i)))
                throw new InvalidConstructionException("mech.bridge.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)), new Vector(0, 0, 0));
        }

        // Don't toggle the end points
        toggle.contract(BukkitUtil.toVector(SignUtil.getBack(trigger)), BukkitUtil.toVector(SignUtil.getFront(trigger)));

        // this is kinda funky, but we only check one position
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Block hinge = proximalBaseCenter.getRelative(SignUtil.getFacing(trigger));

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (BlockUtil.isBlockReplacable(hinge.getTypeId()) && proximalBaseCenter.getType() != hinge.getType())
            return closeBridge(trigger, farSide, proximalBaseCenter, toggle, player);
        else
            return openBridge(trigger, farSide, proximalBaseCenter, toggle);
    }

    public boolean openBridge(Block sign, Block farSide, Block base, CuboidRegion toggle) {

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        ChangedSign other = BukkitUtil.toChangedSign(farSide);
        for (Vector bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            Material oldType = b.getType();
            if (b.getType() == base.getType() || BlockUtil.isBlockReplacable(b.getTypeId())) {
                b.setType(Material.AIR);
                if (CraftBookPlugin.inst().getConfiguration().safeDestruction) {
                    if (oldType == base.getType()) {
                        addBlocks(s, other, 1);
                    }
                }
            }
        }

        return true;
    }

    public boolean closeBridge(Block sign, Block farSide, Block base, CuboidRegion toggle, LocalPlayer player) {

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        ChangedSign other = BukkitUtil.toChangedSign(farSide);
        for (Vector bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (BlockUtil.isBlockReplacable(b.getTypeId())) {
                if (CraftBookPlugin.inst().getConfiguration().safeDestruction) {
                    if (hasEnoughBlocks(s, other)) {
                        b.setType(base.getType());
                        b.setData(base.getData());
                        removeBlocks(s, other, 1);
                    } else {
                        if (player != null) {
                            player.printError("mech.not-enough-blocks");
                        }
                        return false;
                    }
                } else {
                    b.setType(base.getType());
                    b.setData(base.getData());
                }
            }
        }

        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!BukkitUtil.toChangedSign(event.getBlock()).getLine(1).equals("[Bridge]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        ChangedSign sign = null, other;

        if (SignUtil.isSign(event.getBlock()))
            sign = BukkitUtil.toChangedSign(event.getBlock());

        if (sign == null) return;

        other = BukkitUtil.toChangedSign(getFarSign(event.getBlock()));

        if (hasEnoughBlocks(sign, other)) {
            Block bridge;
            try {
                bridge = getBridgeBase(event.getBlock());
                ItemStack toDrop = new ItemStack(bridge.getType(), getBlocks(sign, other), bridge.getData());
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
            } catch (InvalidMechanismException e) {
                if(e.getMessage() != null)
                    player.printError(e.getMessage());
            }
        }
    }

    public boolean removeBlocks(ChangedSign s, ChangedSign other, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s, other) - amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public boolean addBlocks(ChangedSign s, ChangedSign other, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s, other) + amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public void setBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return;
        s.setLine(0, String.valueOf(amount));
        s.update(false);
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

    public boolean hasEnoughBlocks(ChangedSign s, ChangedSign other) {

        return s.getLine(0).equalsIgnoreCase("infinite") || getBlocks(s, other) > 0;
    }
}