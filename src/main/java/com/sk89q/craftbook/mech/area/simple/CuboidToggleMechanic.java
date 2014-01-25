package com.sk89q.craftbook.mech.area.simple;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.pipe.PipeFinishEvent;
import com.sk89q.craftbook.circuits.pipe.PipePutEvent;
import com.sk89q.craftbook.circuits.pipe.PipeSuckEvent;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * A class that can be a mechanic that toggles a cuboid. This is basically either Door or Bridge.
 */
public abstract class CuboidToggleMechanic extends AbstractCraftBookMechanic {

    public abstract Block getFarSign(Block trigger);

    public abstract Block getBlockBase(Block trigger) throws InvalidMechanismException;

    public abstract CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidMechanismException;

    public abstract boolean isApplicableSign(String line);

    public boolean open(Block sign, Block farSide, Block base, CuboidRegion toggle) {

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        ChangedSign other = BukkitUtil.toChangedSign(farSide);
        for (Vector bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            Material oldType = b.getType();
            if (b.getType() == base.getType() || BlockUtil.isBlockReplacable(b.getType())) {
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

    public boolean close(Block sign, Block farSide, Block base, CuboidRegion toggle, LocalPlayer player) {

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        ChangedSign other = BukkitUtil.toChangedSign(farSide);
        for (Vector bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (BlockUtil.isBlockReplacable(b.getType())) {
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeFinish(PipeFinishEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getOrigin())) return;
        ChangedSign sign = BukkitUtil.toChangedSign(event.getOrigin());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<ItemStack>();
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
        ChangedSign sign = BukkitUtil.toChangedSign(event.getPuttingBlock());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<ItemStack>();
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
        ChangedSign sign = BukkitUtil.toChangedSign(event.getSuckedBlock());

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
        if (!isApplicableSign(BukkitUtil.toChangedSign(event.getBlock()).getLine(1))) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        ChangedSign sign = null, other;

        if (SignUtil.isSign(event.getBlock()))
            sign = BukkitUtil.toChangedSign(event.getBlock());

        if (sign == null) return;

        other = BukkitUtil.toChangedSign(getFarSign(event.getBlock()));

        if (hasEnoughBlocks(sign, other)) {
            Block base;
            try {
                base = getBlockBase(event.getBlock());
                ItemStack toDrop = new ItemStack(base.getType(), getBlocks(sign, other), base.getData());
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