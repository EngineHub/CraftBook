/*
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

package org.enginehub.craftbook.mechanics.area;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanic.exception.InvalidMechanismException;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The default bridge mechanism -- signposts on either side of a 3xN plane of (or 1xN plane if 1 on
 * second line) blocks.
 */
public class Bridge extends CuboidToggleMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!event.getLine(1).equalsIgnoreCase("[bridge]") && !event.getLine(1).equalsIgnoreCase("[bridge end]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.bridge.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        if (event.getLine(1).equalsIgnoreCase("[bridge]")) {
            event.setLine(1, "[Bridge]");
            player.printInfo(TranslatableComponent.of("craftbook.bridge.create"));
        } else if (event.getLine(1).equalsIgnoreCase("[bridge end]")) {
            event.setLine(1, "[Bridge End]");
            player.printInfo(TranslatableComponent.of("craftbook.bridge.end-create"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (!EventUtil.passesFilter(event) || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == null) {
            return;
        }

        if (!isApplicableSign(event.getSign().getLine(1))) {
            return;
        }

        CraftBookPlayer player = event.getWrappedPlayer();

        if (!player.hasPermission("craftbook.bridge.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        try {
            if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction) {
                Material heldItemType = event.getPlayer().getInventory().getItem(event.getHand()).getType();
                if (heldItemType != Material.AIR) {
                    Material bridgeType = getOrSetStoredType(event.getClickedBlock());
                    if (bridgeType == heldItemType) {
                        if (!player.hasPermission("craftbook.bridge.restock")) {
                            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                                player.printError(TranslatableComponent.of("craftbook.bridge.restock-permissions"));
                            }
                            return;
                        }

                        int heldAmount = event.getPlayer().getInventory().getItem(event.getHand()).getAmount();

                        int amount = 1;
                        if (event.getPlayer().isSneaking() && heldAmount >= 5) {
                            amount = 5;
                        }

                        addToStoredBlockCount(event.getSign().getSign(), amount);

                        if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
                            event.getPlayer().getInventory().getItem(event.getHand()).subtract(amount);
                        }

                        player.printInfo(TranslatableComponent.of("craftbook.bridge.restock"));
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            event.setCancelled(true);

            if (flipState(event.getClickedBlock())) {
                player.printInfo(TranslatableComponent.of("craftbook.bridge.toggle"));
            }
        } catch (InvalidMechanismException e) {
            if (e.getRichMessage() != null) {
                player.printError(e.getRichMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {
        if (!allowRedstone || event.isMinor() || !EventUtil.passesFilter(event)) {
            return;
        }

        if (!SignUtil.isSign(event.getBlock()) || !isApplicableSign(CraftBookBukkitUtil.toChangedSign(event.getBlock()).getLine(1))) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
            try {
                flipState(event.getBlock());
            } catch (InvalidMechanismException ignored) {
                // Ignore this, as there's nowhere to print this message.
            }
        }, 2L);
    }

    @Override
    public Block getBlockBase(Block sign) throws InvalidMechanismException {
        Block proximalBaseCenter = sign.getRelative(BlockFace.UP);
        if (sign.getY() < sign.getWorld().getMaxHeight() - 1 && Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(proximalBaseCenter.getBlockData()))) {
            return proximalBaseCenter; // On Top
        }

        // If we've reached this point nothing was found on the top, check the bottom
        proximalBaseCenter = sign.getRelative(BlockFace.DOWN);
        if (sign.getY() > sign.getWorld().getMinHeight() && Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(proximalBaseCenter.getBlockData()))) {
            return proximalBaseCenter; // it's below
        }

        proximalBaseCenter = sign.getRelative(SignUtil.getBack(sign));
        if (Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(proximalBaseCenter.getBlockData()))) {
            return proximalBaseCenter; // it's behind
        }

        throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.unusable-material"));
    }

    @Override
    public Block getFarSign(Block nearSign) {
        BlockFace dir = SignUtil.getFacing(nearSign);
        Block farSide = nearSign.getRelative(dir);
        Material nearType = nearSign.getType();

        for (int i = 0; i <= maxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (farSide.getType() == nearType) {
                String otherSignText = CraftBookBukkitUtil.toChangedSign(farSide).getLine(1);
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
        double distance = proximalBaseCenter.getLocation().distanceSquared(distalBaseCenter.getLocation());
        if (distance <= 2 * 2) {
            throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.too-short"));
        }

        CuboidRegion toggle = new CuboidRegion(BukkitAdapter.asBlockVector(proximalBaseCenter.getLocation()), BukkitAdapter.asBlockVector(distalBaseCenter.getLocation()));
        Material expectedType = getOrSetStoredType(trigger);

        if (distalBaseCenter.getType() != proximalBaseCenter.getType()) {
            throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.different-materials"));
        }

        // Expand Left
        for (int i = 1; i < maxWidth; i++) {
            Material proximalOffsetType = proximalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getType();
            if (proximalOffsetType != expectedType) {
                // Check if this is a valid bridge block first.
                break;
            }
            if (distalBaseCenter.getRelative(SignUtil.getLeft(trigger), i).getType() != proximalOffsetType) {
                throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.different-materials"));
            }
            toggle.expand(CraftBookBukkitUtil.toVector(SignUtil.getLeft(trigger)), BlockVector3.ZERO);
        }

        // Expand Right
        for (int i = 1; i < maxWidth; i++) {
            Material proximalOffsetType = proximalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getType();
            if (proximalOffsetType != expectedType) {
                // Check if this is a valid bridge block first.
                break;
            }
            if (distalBaseCenter.getRelative(SignUtil.getRight(trigger), i).getType() != proximalOffsetType) {
                throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.different-materials"));
            }
            toggle.expand(CraftBookBukkitUtil.toVector(SignUtil.getRight(trigger)), BlockVector3.ZERO);
        }

        // Don't toggle the end points
        toggle.contract(CraftBookBukkitUtil.toVector(SignUtil.getBack(trigger)), CraftBookBukkitUtil.toVector(SignUtil.getFront(trigger)));

        return toggle;
    }

    public boolean flipState(Block trigger) throws InvalidMechanismException {
        if (!SignUtil.isCardinal(trigger)) {
            return false;
        }

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        Block proximalBaseCenter = getBlockBase(trigger);
        BlockData proximalBaseData = proximalBaseCenter.getBlockData();

        Material bridgeType = getOrSetStoredType(trigger);
        if (bridgeType != proximalBaseData.getMaterial()) {
            throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.different-materials"));
        }

        // Find the other side
        Block farSide = getFarSign(trigger);

        if (farSide == null || farSide.getType() != trigger.getType()) {
            throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.missing-other-sign"));
        }

        // Check the other side's base blocks for matching type
        BlockFace face = trigger.getFace(proximalBaseCenter);
        if (face != null && face != BlockFace.UP && face != BlockFace.DOWN) {
            face = face.getOppositeFace();
        }

        Block distalBaseCenter = farSide.getRelative(face);
        if (!distalBaseCenter.getBlockData().matches(proximalBaseData)) {
            throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.different-materials"));
        }

        // Select the togglable region
        CuboidRegion toggle = getCuboidArea(trigger, proximalBaseCenter, distalBaseCenter);

        // this is kinda funky, but we only check one position
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Material hingeType = proximalBaseCenter.getRelative(SignUtil.getFacing(trigger)).getType();

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (BlockUtil.isBlockReplacable(hingeType) && proximalBaseData.getMaterial() != hingeType) {
            boolean closeSuccess = close((Sign) trigger.getState(false), (Sign) farSide.getState(false), proximalBaseData, toggle);
            if (!closeSuccess) {
                throw new InvalidMechanismException(TranslatableComponent.of("craftbook.bridge.not-enough-blocks"));
            }
            return true;
        } else {
            return open((Sign) trigger.getState(false), proximalBaseData, toggle);
        }
    }

    @Override
    public boolean isApplicableSign(String line) {
        return line.equals("[Bridge]");
    }

    private boolean allowRedstone;
    private int maxLength;
    private int maxWidth;
    private List<BaseBlock> blocks;

    public List<String> getDefaultBlocks() {
        List<String> materials = new ArrayList<>();
        materials.add(BlockTypes.COBBLESTONE.getId());
        materials.add(BlockTypes.GLASS.getId());
        materials.addAll(BlockCategories.PLANKS.getAll().stream().map(BlockType::getId).toList());
        materials.addAll(BlockCategories.SLABS.getAll().stream().map(BlockType::getId).toList());
        return materials;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        super.loadFromConfiguration(config);

        config.setComment("allow-redstone", "Allow bridges to be toggled via redstone.");
        allowRedstone = config.getBoolean("allow-redstone", true);

        config.setComment("max-length", "Maximum length of a bridge.");
        maxLength = config.getInt("max-length", 30);

        config.setComment("max-width", "Max width either side. 5 = 11, 1 in middle, 5 on either side.");
        maxWidth = config.getInt("max-width", 5);

        config.setComment("blocks", "List of blocks that a bridge can be made out of.");
        blocks = BlockParser.getBlocks(config.getStringList("blocks", getDefaultBlocks().stream().sorted(String::compareToIgnoreCase).toList()), true);
    }
}
