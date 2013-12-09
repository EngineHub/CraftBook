package com.sk89q.craftbook.mech.area.simple;

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
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InvalidConstructionException;
import com.sk89q.craftbook.util.exceptions.InvalidDirectionException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.UnacceptableMaterialException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * Door.
 *
 * @author turtle9598
 */
public class Door extends CuboidToggleMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[door]") && !event.getLine(1).equalsIgnoreCase("[door up]") && !event.getLine(1).equalsIgnoreCase("[door down]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.mech.door")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if (event.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.door.infinite"))
            event.setLine(0, "0");
        else if (!event.getLine(0).equalsIgnoreCase("infinite"))
            event.setLine(0, "0");

        player.print("mech.door.create");

        if(event.getLine(1).equalsIgnoreCase("[door]"))
            event.setLine(1, "[Door]");
        else if(event.getLine(1).equalsIgnoreCase("[door up]"))
            event.setLine(1, "[Door Up]");
        else if(event.getLine(1).equalsIgnoreCase("[door down]"))
            event.setLine(1, "[Door Down]");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isApplicableSign(BukkitUtil.toChangedSign(event.getClickedBlock()).getLine(1))) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.door.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        try {
            ChangedSign sign = event.getSign();

            if (CraftBookPlugin.inst().getConfiguration().safeDestruction && sign != null && !sign.getLine(0).equalsIgnoreCase("infinite"))
                if (event.getPlayer().getItemInHand() != null)
                    if (getBlockBase(event.getClickedBlock()).getType() == event.getPlayer().getItemInHand().getType()) {

                        if (!player.hasPermission("craftbook.mech.door.restock")) {
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
                player.print("mech.door.toggle");
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

    public boolean flipState(Block trigger, LocalPlayer player) throws InvalidMechanismException {

        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();

        ChangedSign sign = BukkitUtil.toChangedSign(trigger);

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        Block proximalBaseCenter = getBlockBase(trigger);

        // Find the other side
        Block farSide = getFarSign(trigger);

        if (farSide.getType() != trigger.getType()) throw new InvalidConstructionException("mech.door.other-sign");

        // Check the other side's base blocks for matching type
        Block distalBaseCenter = null;
        if (sign.getLine(1).equalsIgnoreCase("[Door Up]")) {
            distalBaseCenter = farSide.getRelative(BlockFace.DOWN);
        } else if (sign.getLine(1).equalsIgnoreCase("[Door Down]")) {
            distalBaseCenter = farSide.getRelative(BlockFace.UP);
        }
        if (!BlockUtil.areBlocksIdentical(distalBaseCenter, proximalBaseCenter))
            throw new InvalidConstructionException("mech.door.material");

        // Select the togglable region
        CuboidRegion toggle = getCuboidArea(trigger, proximalBaseCenter, distalBaseCenter);

        // this is kinda funky, but we only check one position
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Block hinge;

        if (BukkitUtil.toChangedSign(trigger).getLine(1).equals("[Door Up]")) {
            hinge = proximalBaseCenter.getRelative(BlockFace.UP);
        } else {
            hinge = proximalBaseCenter.getRelative(BlockFace.DOWN);
        }

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (BlockUtil.isBlockReplacable(hinge.getTypeId()) && proximalBaseCenter.getType() != hinge.getType())
            return close(trigger, farSide, proximalBaseCenter, toggle, player);
        else
            return open(trigger, farSide, proximalBaseCenter, toggle);

    }

    @Override
    public Block getFarSign(Block trigger) {
        // Find the other side
        Block otherSide = null;

        ChangedSign sign = BukkitUtil.toChangedSign(trigger);

        if (sign.getLine(1).equals("[Door Up]")) {
            otherSide = trigger.getRelative(BlockFace.UP);
        } else if (sign.getLine(1).equals("[Door Down]")) {
            otherSide = trigger.getRelative(BlockFace.DOWN);
        }
        for (int i = 0; i <= CraftBookPlugin.inst().getConfiguration().doorMaxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (otherSide.getType() == Material.SIGN_POST) {
                String otherSignText = BukkitUtil.toChangedSign(otherSide).getLine(1);
                if (isApplicableSign(otherSignText))
                    break;
                if ("[Door]".equals(otherSignText))
                    break;
            }

            if (sign.getLine(1).equals("[Door Up]")) {
                otherSide = otherSide.getRelative(BlockFace.UP);
            } else if (sign.getLine(1).equals("[Door Down]")) {
                otherSide = otherSide.getRelative(BlockFace.DOWN);
            }
        }

        return otherSide;
    }

    @Override
    public Block getBlockBase(Block trigger) throws InvalidMechanismException {
        ChangedSign s = BukkitUtil.toChangedSign(trigger);

        Block proximalBaseCenter = null;

        if (s.getLine(1).equalsIgnoreCase("[Door Up]")) {
            proximalBaseCenter = trigger.getRelative(BlockFace.UP);
        } else if (s.getLine(1).equalsIgnoreCase("[Door Down]")) {
            proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
        } else throw new InvalidConstructionException("Sign is incorrectly made.");

        if (CraftBookPlugin.inst().getConfiguration().doorBlocks.contains(new ItemInfo(proximalBaseCenter)))
            return proximalBaseCenter;
        else throw new UnacceptableMaterialException("mech.door.unusable");
    }

    @Override
    public CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidConstructionException {
        // Select the togglable region
        CuboidRegion toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter), BukkitUtil.toVector(distalBaseCenter));
        ChangedSign sign = BukkitUtil.toChangedSign(trigger);
        int left, right;
        try {
            left = Math.max(0, Math.min(CraftBookPlugin.inst().getConfiguration().doorMaxWidth, Integer.parseInt(sign.getLine(2))));
        } catch (Exception e) {
            left = 1;
        }
        try {
            right = Math.max(0, Math.min(CraftBookPlugin.inst().getConfiguration().doorMaxWidth, Integer.parseInt(sign.getLine(3))));
        } catch (Exception e) {
            right = 1;
        }

        // Expand Left
        for (int i = 0; i < left; i++) {
            if(distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getType() != proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getType() && distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getData() != proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getData())
                throw new InvalidConstructionException("mech.door.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)), new Vector(0, 0, 0));
        }

        // Expand Right
        for (int i = 0; i < right; i++) {
            if(distalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getType() != proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getType() && distalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getData() != proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getData())
                throw new InvalidConstructionException("mech.door.material");
            toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)), new Vector(0, 0, 0));
        }

        // Don't toggle the end points
        toggle.contract(BukkitUtil.toVector(BlockFace.UP), BukkitUtil.toVector(BlockFace.DOWN));

        return toggle;
    }

    @Override
    public boolean isApplicableSign(String line) {
        return line.equals("[Door Up]") || line.equals("[Door Down]");
    }
}