package com.sk89q.craftbook.circuits.gates.world.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonBaseMaterial;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.PipeInputIC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class Distributer extends AbstractSelfTriggeredIC implements PipeInputIC {

    public Distributer(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Block chestBlock;
    int right, left;
    int currentIndex;

    @Override
    public void load() {

        try {

            currentIndex = Integer.parseInt(getLine(3));
        }
        catch(Exception e) {
            currentIndex = -1;
        }
        left = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getLine(2))[0]);
        right = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getLine(2))[1]);
        chestBlock = getBackBlock().getRelative(0, 1, 0);
    }

    @Override
    public String getTitle() {

        return "Distributer";
    }

    @Override
    public String getSignTitle() {

        return "DISTRIBUTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, distribute());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, distribute());
    }

    public boolean distribute() {

        boolean returnValue = false;

        for (Entity en : BukkitUtil.toSign(getSign()).getChunk().getEntities()) {
            if (!(en instanceof Item)) {
                continue;
            }
            Item item = (Item) en;
            ItemStack stack = item.getItemStack();
            if (!ItemUtil.isStackValid(stack) || item.isDead() || !item.isValid()) {
                continue;
            }
            Location location = item.getLocation();
            int ix = location.getBlockX();
            int iy = location.getBlockY();
            int iz = location.getBlockZ();
            if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {

                BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
                Block b;

                if (goRight()) {
                    b = SignUtil.getRightBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
                } else {
                    b = SignUtil.getLeftBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
                }

                boolean pipes = false;

                if (b.getTypeId() == BlockID.PISTON_STICKY_BASE) {

                    PistonBaseMaterial p = (PistonBaseMaterial) b.getState().getData();
                    Block fac = b.getRelative(p.getFacing());
                    if (fac.getLocation().equals(BukkitUtil.toSign(getSign()).getBlock().getRelative(back)
                            .getLocation())) {

                        List<ItemStack> items = new ArrayList<ItemStack>();
                        items.add(item.getItemStack());
                        if (CircuitCore.inst().getPipeFactory() != null)
                            if (CircuitCore.inst().getPipeFactory().detectWithItems(BukkitUtil.toWorldVector(b), items) != null) {
                                item.remove();
                                pipes = true;
                                returnValue = true;
                            }
                    }
                }

                if (!pipes) item.teleport(b.getLocation().add(0.5, 0.5, 0.5));

                returnValue = true;
            }
        }
        return returnValue;
    }

    public void distributeItem(ItemStack item) {

        BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
        Block b;

        if (goRight()) {
            b = SignUtil.getRightBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
        } else {
            b = SignUtil.getLeftBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
        }

        boolean pipes = false;

        if (b.getTypeId() == BlockID.PISTON_STICKY_BASE) {

            PistonBaseMaterial p = (PistonBaseMaterial) b.getState().getData();
            Block fac = b.getRelative(p.getFacing());
            if (fac.getLocation().equals(BukkitUtil.toSign(getSign()).getBlock().getRelative(back).getLocation())) {

                List<ItemStack> items = new ArrayList<ItemStack>();
                items.add(item);
                if (CircuitCore.inst().getPipeFactory() != null)
                    if (CircuitCore.inst().getPipeFactory().detectWithItems(BukkitUtil.toWorldVector(b), items) != null) {
                        pipes = true;
                    }
            }
        }

        if (!pipes) {
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), item);
        }
    }

    public boolean goRight() {

        currentIndex++;
        getSign().setLine(3, String.valueOf(currentIndex));
        if (currentIndex > left && currentIndex <= left+right)
            return true;
        else if (currentIndex <= left)
            return false;
        else {
            currentIndex = 0;
            getSign().setLine(3, String.valueOf(currentIndex));
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Distributer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Distributes items to right and left based on sign.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"left quantity:right quantity", "Current distribution status"};
            return lines;
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            try {
                Integer.parseInt(RegexUtil.COLON_PATTERN.split(sign.getLine(2))[0]);
                Integer.parseInt(RegexUtil.COLON_PATTERN.split(sign.getLine(2))[1]);
            }
            catch(ArrayIndexOutOfBoundsException e) {

                throw new ICVerificationException("You need to specify both left and right quantities!");
            }
            catch(NumberFormatException e) {

                throw new ICVerificationException("Invalid quantities!");
            }
        }
    }

    @Override
    public List<ItemStack> onPipeTransfer(BlockWorldVector pipe, List<ItemStack> items) {

        for (ItemStack item : items) { if (ItemUtil.isStackValid(item)) distributeItem(item); }

        return new ArrayList<ItemStack>();
    }
}
