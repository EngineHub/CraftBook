package com.sk89q.craftbook.mechanics.area.simple;

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

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Door.
 *
 * @author turtle9598
 */
public class Door extends CuboidToggleMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[door]") && !event.getLine(1).equalsIgnoreCase("[door up]") && !event.getLine(1).equalsIgnoreCase("[door down]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isApplicableSign(CraftBookBukkitUtil.toChangedSign(event.getClickedBlock()).getLine(1))) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.door.use")) {
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
                if (event.getPlayer().getItemInHand().getType() != Material.AIR)
                    if (getBlockBase(event.getClickedBlock()).getType() == event.getPlayer().getItemInHand().getType() && getBlockBase(event.getClickedBlock()).getData() == event.getPlayer().getItemInHand().getData().getData()) {

                        if (!player.hasPermission("craftbook.mech.door.restock")) {
                            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                                player.printError("mech.restock-permission");
                            return;
                        }

                        int amount = 1;
                        if (event.getPlayer().isSneaking() && event.getPlayer().getItemInHand().getAmount() >= 5) {
                            amount = 5;
                        }
                        addBlocks(sign, CraftBookBukkitUtil.toChangedSign(getFarSign(event.getClickedBlock())), amount);

                        if (enforceType) {
                            BlockType blockType = player.getItemInHand(HandSide.MAIN_HAND).getType().getBlockType();
                            sign.setLine(0, sign.getLine(0) + ',' + BlockSyntax.toMinifiedId(blockType.getFuzzyMatcher()));
                            sign.update(false);
                        }

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!allowRedstone) return;
        if (event.isMinor()) return;

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!isApplicableSign(CraftBookBukkitUtil.toChangedSign(event.getBlock()).getLine(1))) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
            try {
                flipState(event.getBlock(), null);
            } catch (InvalidMechanismException e) {
            }
        }, 2L);
    }

    public boolean flipState(Block trigger, CraftBookPlayer player) throws InvalidMechanismException {

        if (!SignUtil.isCardinal(trigger)) throw new InvalidMechanismException();

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(trigger);

        // Attempt to detect whether the door is above or below the sign,
        // first assuming that the bridge is above
        Block proximalBaseCenter = getBlockBase(trigger);

        BlockData doorType = getBlockType(trigger);
        if (!BlockUtil.areBlocksIdentical(proximalBaseCenter, doorType)) {
            throw new InvalidMechanismException("mech.door.material");
        }

        // Find the other side
        Block farSide = getFarSign(trigger);

        if (farSide.getType() != trigger.getType()) throw new InvalidMechanismException("mech.door.other-sign");

        // Check the other side's base blocks for matching type
        Block distalBaseCenter = null;
        if (sign.getLine(1).equalsIgnoreCase("[Door Up]")) {
            distalBaseCenter = farSide.getRelative(BlockFace.DOWN);
        } else if (sign.getLine(1).equalsIgnoreCase("[Door Down]")) {
            distalBaseCenter = farSide.getRelative(BlockFace.UP);
        }
        if (!BlockUtil.areBlocksIdentical(distalBaseCenter, proximalBaseCenter))
            throw new InvalidMechanismException("mech.door.material");

        // Select the togglable region
        CuboidRegion toggle = getCuboidArea(trigger, proximalBaseCenter, distalBaseCenter);

        // this is kinda funky, but we only check one position
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Block hinge;

        if (sign.getLine(1).equals("[Door Up]")) {
            hinge = proximalBaseCenter.getRelative(BlockFace.UP);
        } else {
            hinge = proximalBaseCenter.getRelative(BlockFace.DOWN);
        }

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (BlockUtil.isBlockReplacable(hinge.getType()) && proximalBaseCenter.getType() != hinge.getType())
            return close(trigger, farSide, doorType, toggle, player);
        else
            return open(trigger, farSide, doorType, toggle);

    }

    @Override
    public Block getFarSign(Block trigger) {
        // Find the other side
        Block otherSide = null;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(trigger);

        if (sign.getLine(1).equals("[Door Up]")) {
            otherSide = trigger.getRelative(BlockFace.UP);
        } else if (sign.getLine(1).equals("[Door Down]")) {
            otherSide = trigger.getRelative(BlockFace.DOWN);
        }
        for (int i = 0; i <= maxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (SignUtil.isSign(otherSide)) {
                String otherSignText = CraftBookBukkitUtil.toChangedSign(otherSide).getLine(1);
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
        ChangedSign s = CraftBookBukkitUtil.toChangedSign(trigger);

        Block proximalBaseCenter = null;

        if (s.getLine(1).equalsIgnoreCase("[Door Up]")) {
            proximalBaseCenter = trigger.getRelative(BlockFace.UP);
        } else if (s.getLine(1).equalsIgnoreCase("[Door Down]")) {
            proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
        } else throw new InvalidMechanismException("Sign is incorrectly made.");

        if (Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(proximalBaseCenter.getBlockData())))
            return proximalBaseCenter;
        else throw new InvalidMechanismException("mech.door.unusable");
    }

    @Override
    public CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidMechanismException {
        double distance = proximalBaseCenter.getLocation().distanceSquared(distalBaseCenter.getLocation());
        if (distance <= 2*2) {
            throw new InvalidMechanismException("Door too short!");
        }
        // Select the togglable region
        CuboidRegion toggle = new CuboidRegion(CraftBookBukkitUtil.toVector(proximalBaseCenter), CraftBookBukkitUtil.toVector(distalBaseCenter));
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(trigger);
        int left, right;
        try {
            left = Math.max(0, Math.min(maxWidth, Integer.parseInt(sign.getLine(2))));
        } catch (Exception e) {
            left = 1;
        }
        try {
            right = Math.max(0, Math.min(maxWidth, Integer.parseInt(sign.getLine(3))));
        } catch (Exception e) {
            right = 1;
        }

        // Expand Left
        for (int i = 0; i < left; i++) {
            if(distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getType() != proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getType())
                throw new InvalidMechanismException("mech.door.material");
            toggle.expand(CraftBookBukkitUtil.toVector(SignUtil.getLeft(trigger)), BlockVector3.ZERO);
        }

        // Expand Right
        for (int i = 0; i < right; i++) {
            if(distalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getType() != proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getType())
                throw new InvalidMechanismException("mech.door.material");
            toggle.expand(CraftBookBukkitUtil.toVector(SignUtil.getRight(trigger)), BlockVector3.ZERO);
        }

        // Don't toggle the end points
        toggle.contract(CraftBookBukkitUtil.toVector(BlockFace.UP), CraftBookBukkitUtil.toVector(BlockFace.DOWN));

        return toggle;
    }

    @Override
    public boolean isApplicableSign(String line) {
        return line.equals("[Door Up]") || line.equals("[Door Down]");
    }

    private boolean allowRedstone;
    private int maxLength;
    private int maxWidth;
    private List<BaseBlock> blocks;

    public List<String> getDefaultBlocks() {
        List<String> materials = new ArrayList<>();
        materials.add(BlockTypes.COBBLESTONE.getId());
        materials.add(BlockTypes.GLASS.getId());
        materials.addAll(BlockCategories.PLANKS.getAll().stream().map(BlockType::getId).collect(Collectors.toList()));
        materials.addAll(BlockCategories.SLABS.getAll().stream().map(BlockType::getId).collect(Collectors.toList()));
        return materials;
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {
        super.loadConfiguration(config, path);

        config.setComment(path + "allow-redstone", "Allow doors to be toggled via redstone.");
        allowRedstone = config.getBoolean(path + "allow-redstone", true);

        config.setComment(path + "max-length", "The maximum length(height) of a door.");
        maxLength = config.getInt(path + "max-length", 30);

        config.setComment(path + "max-width", "Max width either side. 5 = 11, 1 in middle, 5 on either side");
        maxWidth = config.getInt(path + "max-width", 5);

        config.setComment(path + "blocks", "A list of blocks that a door can be made out of.");
        blocks = BlockSyntax.getBlocks(config.getStringList(path + "blocks",
                getDefaultBlocks().stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList())), true);
    }
}