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

package com.sk89q.craftbook.mech.area.simple;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * The default bridge mechanism -- signposts on either side of a 3xN plane of (or 1xN plane if 1 on second line) blocks.
 *
 * @author hash
 */
public class Bridge extends CuboidToggleMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isApplicableSign(BukkitUtil.toChangedSign(event.getClickedBlock()).getLine(1))) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.bridge.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        try {
            ChangedSign sign = event.getSign();

            if (CraftBookPlugin.inst().getConfiguration().safeDestruction && sign != null && !sign.getLine(0).equalsIgnoreCase("infinite"))
                if (event.getPlayer().getItemInHand() != null)
                    if (getBlockBase(event.getClickedBlock()).getType() == event.getPlayer().getItemInHand().getType() && getBlockBase(event.getClickedBlock()).getData() == event.getPlayer().getItemInHand().getData().getData()) {

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!CraftBookPlugin.inst().getConfiguration().bridgeAllowRedstone) return;
        if (event.isMinor()) return;

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!isApplicableSign(BukkitUtil.toChangedSign(event.getBlock()).getLine(1))) return;

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

    @Override
    public Block getBlockBase(Block trigger) throws InvalidMechanismException {
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
        else throw new InvalidMechanismException("mech.bridge.unusable");
    }

    @Override
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

    @Override
    public CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidMechanismException {

        CuboidRegion toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter), BukkitUtil.toVector(distalBaseCenter));
        ChangedSign sign = BukkitUtil.toChangedSign(trigger);
        int left, right;
        try {
            left = Math.max(0, Math.min(CraftBookPlugin.inst().getConfiguration().bridgeMaxWidth, Integer.parseInt(sign.getLine(2))));
        } catch (Exception e) {
            left = 1;
        }
        try {
            right = Math.max(0, Math.min(CraftBookPlugin.inst().getConfiguration().bridgeMaxWidth, Integer.parseInt(sign.getLine(3))));
        } catch (Exception e) {
            right = 1;
        }

        // Expand Left
        for (int i = 0; i < left; i++) {
            if(!BlockUtil.areBlocksIdentical(distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i), proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i)))
                throw new InvalidMechanismException("mech.bridge.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)), new Vector(0, 0, 0));
        }

        // Expand Right
        for (int i = 0; i < right; i++) {
            if(!BlockUtil.areBlocksIdentical(distalBaseCenter.getRelative(SignUtil.getRight(trigger), i), proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i)))
                throw new InvalidMechanismException("mech.bridge.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)), new Vector(0, 0, 0));
        }

        // Don't toggle the end points
        toggle.contract(BukkitUtil.toVector(SignUtil.getBack(trigger)), BukkitUtil.toVector(SignUtil.getFront(trigger)));

        return toggle;
    }

    public boolean flipState(Block trigger, LocalPlayer player) throws InvalidMechanismException {

        if (!SignUtil.isCardinal(trigger)) throw new InvalidMechanismException();

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        Block proximalBaseCenter = getBlockBase(trigger);

        // Find the other side
        Block farSide = getFarSign(trigger);

        if (farSide.getType() != trigger.getType()) throw new InvalidMechanismException("mech.bridge.other-sign");

        // Check the other side's base blocks for matching type
        BlockFace face = trigger.getFace(proximalBaseCenter);
        if(face != BlockFace.UP && face != BlockFace.DOWN) face = face.getOppositeFace();
        Block distalBaseCenter = farSide.getRelative(face);
        if (!BlockUtil.areBlocksIdentical(distalBaseCenter, proximalBaseCenter))
            throw new InvalidMechanismException("mech.bridge.material");

        // Select the togglable region
        CuboidRegion toggle = getCuboidArea(trigger, proximalBaseCenter, distalBaseCenter);

        // this is kinda funky, but we only check one position
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Block hinge = proximalBaseCenter.getRelative(SignUtil.getFacing(trigger));

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (BlockUtil.isBlockReplacable(hinge.getType()) && proximalBaseCenter.getType() != hinge.getType())
            return close(trigger, farSide, proximalBaseCenter, toggle, player);
        else
            return open(trigger, farSide, proximalBaseCenter, toggle);
    }

    @Override
    public boolean isApplicableSign(String line) {
        return line.equals("[Bridge]");
    }
}