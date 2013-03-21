package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class Irrigator extends AbstractSelfTriggeredIC {

    public Irrigator(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Block centre;
    Vector radius;

    @Override
    public void load() {

        if (getLine(2).contains("=")) {
            centre = ICUtil.parseBlockLocation(getSign(), 2);
        } else {
            centre = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        }
        radius = ICUtil.parseRadius(getSign());
    }

    @Override
    public String getTitle() {

        return "Irrigator";
    }

    @Override
    public String getSignTitle() {

        return "IRRIGATOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, irrigate());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, irrigate());
    }

    public boolean irrigate() {

        for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
            for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                    int rx = centre.getX() - x;
                    int ry = centre.getY() - y;
                    int rz = centre.getZ() - z;
                    Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                    if (b.getTypeId() == BlockID.SOIL && b.getData() < 0x1) {
                        if (consumeWater()) {
                            b.setData((byte) 0x8, false);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean consumeWater() {

        Block chest = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (chest.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(BlockID.WATER, 1));
            if (over.isEmpty()) return true;
            over = c.getInventory().removeItem(new ItemStack(BlockID.STATIONARY_WATER, 1));
            if (over.isEmpty()) return true;
            over = c.getInventory().removeItem(new ItemStack(ItemID.WATER_BUCKET, 1));
            if (over.isEmpty()) {
                c.getInventory().addItem(new ItemStack(ItemID.BUCKET, 1));
                return true;
            }
        } else if (chest.getTypeId() == BlockID.WATER || chest.getTypeId() == BlockID.STATIONARY_WATER) {
            chest.setTypeId(0);
            return true;
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Irrigator(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Irrigates nearby farmland using water in above chest.";
        }

        @Override
        public String getLongDescription() {

            return "The Irrigator IC uses either water blocks or water buckets from the chest above it, and irrigates a block of farmland in the radius specified by line 3. Each farmland block requires either 1 bucket of water or 1 water block to become fully irrigated.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"+oradius=x:y:z offset", null};
            return lines;
        }
    }
}