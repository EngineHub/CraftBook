/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package org.enginehub.craftbook.mechanics.area.simple;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanic.exception.InvalidMechanismException;
import org.enginehub.craftbook.mechanics.pipe.PipeFinishEvent;
import org.enginehub.craftbook.mechanics.pipe.PipePutEvent;
import org.enginehub.craftbook.mechanics.pipe.PipeSuckEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class that can be a mechanic that toggles a cuboid. This is basically either Door or Bridge.
 */
public abstract class CuboidToggleMechanic extends AbstractCraftBookMechanic {

    public abstract Block getFarSign(Block trigger);

    public abstract Block getBlockBase(Block trigger) throws InvalidMechanismException;

    public abstract CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidMechanismException;

    public abstract boolean isApplicableSign(String line);

    public static boolean open(Block sign, Block farSide, BlockData type, CuboidRegion toggle) {

        ChangedSign s = CraftBookBukkitUtil.toChangedSign(sign);
        ChangedSign other = CraftBookBukkitUtil.toChangedSign(farSide);
        for (BlockVector3 bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (b.getType() == type.getMaterial() || BlockUtil.isBlockReplacable(b.getType())) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction && (b.getType() == type.getMaterial()))
                    addBlocks(s, other, 1);
                b.setType(Material.AIR);
            }
        }

        return true;
    }

    public static boolean close(Block sign, Block farSide, BlockData data, CuboidRegion toggle, CraftBookPlayer player) {

        ChangedSign s = CraftBookBukkitUtil.toChangedSign(sign);
        ChangedSign other = CraftBookBukkitUtil.toChangedSign(farSide);
        for (BlockVector3 bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (BlockUtil.isBlockReplacable(b.getType())) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction) {
                    if (hasEnoughBlocks(s, other)) {
                        b.setBlockData(data);
                        removeBlocks(s, other, 1);
                    } else {
                        if (player != null) {
                            player.printError("mech.not-enough-blocks");
                        }
                        return false;
                    }
                } else {
                    b.setBlockData(data);
                }
            }
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeFinish(PipeFinishEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getOrigin())) return;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getOrigin());

        if (!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<>();
        try {
            Block base = getBlockBase(event.getOrigin());
            for (ItemStack stack : event.getItems()) {
                if (stack.getType() != base.getType() || stack.getData().getData() != base.getData()) {
                    leftovers.add(stack);
                    continue;
                }

                addBlocks(sign, null, stack.getAmount());
            }

            event.setItems(leftovers);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipePut(PipePutEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getPuttingBlock())) return;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getPuttingBlock());

        if (!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<>();
        try {
            Block base = getBlockBase(event.getPuttingBlock());
            for (ItemStack stack : event.getItems()) {
                if (stack.getType() != base.getType() || stack.getData().getData() != base.getData()) {
                    leftovers.add(stack);
                    continue;
                }

                addBlocks(sign, null, stack.getAmount());
            }

            event.setItems(leftovers);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeSuck(PipeSuckEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getSuckedBlock())) return;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getSuckedBlock());

        if (!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> items = event.getItems();
        try {
            Block base = getBlockBase(event.getSuckedBlock());
            int blocks = getBlocks(sign, null);
            if (blocks > 0) {
                items.add(new ItemStack(base.getType(), blocks, base.getData()));
                setBlocks(sign, 0);
            }
            event.setItems(items);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!isApplicableSign(CraftBookBukkitUtil.toChangedSign(event.getBlock()).getLine(1)))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        ChangedSign sign = null, other;

        if (SignUtil.isSign(event.getBlock()))
            sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (sign == null) return;

        other = CraftBookBukkitUtil.toChangedSign(getFarSign(event.getBlock()));

        int amount = getBlocks(sign, other);

        if (amount > 0) {
            BlockData base;
            try {
                base = getBlockType(event.getBlock());
                while (amount > 0) {
                    ItemStack toDrop = new ItemStack(base.getMaterial(), Math.min(amount, 64));
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
                    amount -= 64;
                }
            } catch (InvalidMechanismException e) {
                if (e.getMessage() != null)
                    player.printError(e.getMessage());
            }
        }
    }

    public static boolean removeBlocks(ChangedSign s, ChangedSign other, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s, other) - amount;
        if (s.getLine(0).contains(",")) {
            s.setLine(0, String.valueOf(curBlocks) + ',' + s.getLine(0).split(",")[1]);
        } else {
            s.setLine(0, String.valueOf(curBlocks));
        }
        s.update(false);
        return curBlocks >= 0;
    }

    public static boolean addBlocks(ChangedSign s, ChangedSign other, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s, other) + amount;
        if (s.getLine(0).contains(",")) {
            s.setLine(0, String.valueOf(curBlocks) + ',' + s.getLine(0).split(",")[1]);
        } else {
            s.setLine(0, String.valueOf(curBlocks));
        }
        s.update(false);
        return curBlocks >= 0;
    }

    public static void setBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).split(",")[0].equalsIgnoreCase("infinite")) return;
        if (s.getLine(0).contains(",")) {
            s.setLine(0, String.valueOf(amount) + ',' + s.getLine(0).split(",")[1]);
        } else {
            s.setLine(0, String.valueOf(amount));
        }
        s.update(false);
    }

    public static int getBlocks(ChangedSign s, ChangedSign other) {

        if (s.getLine(0).split(",")[0].equalsIgnoreCase("infinite") || other != null && other.getLine(0).split(",")[0].equalsIgnoreCase("infinite"))
            return 0;
        int curBlocks = 0;
        try {
            curBlocks = Integer.parseInt(s.getLine(0).split(",")[0]);
            if (other != null && Objects.equals(getStoredType(other), getStoredType(s))) {
                try {
                    curBlocks += Integer.parseInt(other.getLine(0).split(",")[0]);
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

    public static boolean hasEnoughBlocks(ChangedSign s, ChangedSign other) {

        return s.getLine(0).split(",")[0].equalsIgnoreCase("infinite") || getBlocks(s, other) > 0;
    }

    public static BlockData getStoredType(ChangedSign sign) {
        if (sign.getLine(0).contains(",")) {
            return BukkitAdapter.adapt(BlockParser.getBlock(sign.getLine(0).split(",")[1]));
        }
        return null;
    }

    /**
     * Gets the block type of this mechanic. Usually passes through, but can be used by enforce
     * type.
     *
     * @param block The block location
     * @return The type
     */
    public BlockData getBlockType(Block block) throws InvalidMechanismException {
        if (enforceType) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);
            BlockData type = null;
            if (sign.getLine(0).contains(",")) {
                type = BukkitAdapter.adapt(BlockParser.getBlock(sign.getLine(0).split(",")[1]));
                BlockData realType = this.getBlockBase(block).getBlockData();
                if (type != null && realType.getMaterial() == type.getMaterial()) {
                    return realType;
                }
            }
            if (type == null) {
                type = this.getBlockBase(block).getBlockData();
                sign.setLine(0, sign.getLine(0) + ',' + BlockParser.toMinifiedId(BukkitAdapter.adapt(type).getBlockType().getFuzzyMatcher()));
                sign.update(false);
            }
            return type;
        }

        return this.getBlockBase(block).getBlockData();
    }

    protected boolean enforceType;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enforce-type", "Allow doors to be toggled via redstone.");
        enforceType = config.getBoolean("enforce-type", true);
    }
}