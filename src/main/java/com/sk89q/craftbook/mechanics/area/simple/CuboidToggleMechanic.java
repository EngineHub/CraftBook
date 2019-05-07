package com.sk89q.craftbook.mechanics.area.simple;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.pipe.PipeFinishEvent;
import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;
import com.sk89q.craftbook.mechanics.pipe.PipeSuckEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
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
                if (CraftBookPlugin.inst().getConfiguration().safeDestruction && (b.getType() == type.getMaterial()))
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
                if (CraftBookPlugin.inst().getConfiguration().safeDestruction) {
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

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getOrigin())) return;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getOrigin());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<>();
        try {
            Block base = getBlockBase(event.getOrigin());
            for(ItemStack stack : event.getItems()) {
                if(stack.getType() != base.getType() || stack.getData().getData() != base.getData()) {
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

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getPuttingBlock())) return;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getPuttingBlock());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<>();
        try {
            Block base = getBlockBase(event.getPuttingBlock());
            for(ItemStack stack : event.getItems()) {
                if(stack.getType() != base.getType() || stack.getData().getData() != base.getData()) {
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

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getSuckedBlock())) return;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getSuckedBlock());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> items = event.getItems();
        try {
            Block base = getBlockBase(event.getSuckedBlock());
            int blocks = getBlocks(sign, null);
            if(blocks > 0) {
                items.add(new ItemStack(base.getType(), blocks, base.getData()));
                setBlocks(sign, 0);
            }
            event.setItems(items);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!isApplicableSign(CraftBookBukkitUtil.toChangedSign(event.getBlock()).getLine(1))) return;

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
                while(amount > 0) {
                    ItemStack toDrop = new ItemStack(base.getMaterial(), Math.min(amount, 64));
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
                    amount -= 64;
                }
            } catch (InvalidMechanismException e) {
                if(e.getMessage() != null)
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
            if(other != null && Objects.equals(getStoredType(other), getStoredType(s))) {
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
            return BlockSyntax.getBukkitBlock(sign.getLine(0).split(",")[1]);
        }
        return null;
    }

    /**
     * Gets the block type of this mechanic. Usually passes through, but can be used by enforce type.
     *
     * @param block The block location
     * @return The type
     */
    public BlockData getBlockType(Block block) throws InvalidMechanismException {
        if (enforceType) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);
            BlockData type = null;
            if (sign.getLine(0).contains(",")) {
                type = BlockSyntax.getBukkitBlock(sign.getLine(0).split(",")[1]);
                BlockData realType = this.getBlockBase(block).getBlockData();
                if (type != null && realType.getMaterial() == type.getMaterial()) {
                    return realType;
                }
            }
            if (type == null) {
                type = this.getBlockBase(block).getBlockData();
                sign.setLine(0, sign.getLine(0) + ',' + BlockSyntax.toMinifiedId(BukkitAdapter.adapt(type).getBlockType().getFuzzyMatcher()));
                sign.update(false);
            }
            return type;
        }

        return this.getBlockBase(block).getBlockData();
    }

    protected boolean enforceType;

    @Override
    public void loadConfiguration(YAMLProcessor config, String path) {
        config.setComment(path + "enforce-type", "Allow doors to be toggled via redstone.");
        enforceType = config.getBoolean(path + "enforce-type", true);
    }
}