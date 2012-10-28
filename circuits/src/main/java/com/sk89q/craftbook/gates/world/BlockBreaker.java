package com.sk89q.craftbook.gates.world;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class BlockBreaker extends AbstractIC {

    boolean above;

    public BlockBreaker(Server server, Sign block, boolean above, ICFactory factory) {

        super(server, block, factory);
        this.above = above;
    }

    @Override
    public String getTitle() {

        return "Block Breaker";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK BREAK";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, breakBlock());
        }
    }

    public boolean breakBlock() {

        boolean hasChest = false;
        Block bl = SignUtil.getBackBlock(getSign().getBlock());
        Block chest;
        Block broken;
        if (above) {
            chest = bl.getRelative(0, 1, 0);
            broken = bl.getRelative(0, -1, 0);
        } else {
            chest = bl.getRelative(0, -1, 0);
            broken = bl.getRelative(0, 1, 0);
        }
        if (chest != null && chest.getType() == Material.CHEST) {
            hasChest = true;
        }
        if (broken == null || broken.getTypeId() == 0 || broken.getTypeId() == BlockID.BEDROCK) return false;

        try {
            int blockID = Integer.parseInt(getSign().getLine(2).split(":")[0]);
            if(blockID != broken.getTypeId())
                return false;
            byte data = Byte.parseByte(getSign().getLine(2).split(":")[1]);
            if(data != broken.getData())
                return false;
        }
        catch(Exception e){}

        broken.getDrops();
        for(ItemStack blockstack  : broken.getDrops()) {

            if (hasChest) {
                Chest c = (Chest) chest.getState();
                HashMap<Integer, ItemStack> overflow = c.getInventory().addItem(blockstack);
                if (overflow.size() == 0)
                    continue;
                else {
                    for (Map.Entry<Integer, ItemStack> bit : overflow.entrySet()) {
                        dropItem(bit.getValue());
                    }
                    continue;
                }
            }

            dropItem(blockstack);
        }
        broken.setTypeId(0);


        return true;
    }

    public void dropItem(ItemStack item) {

        getSign().getWorld().dropItem(BlockUtil.getBlockCentre(getSign().getBlock()), item);
    }

    public static class Factory extends AbstractICFactory {

        boolean above;

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(Sign sign) {

            return new BlockBreaker(getServer(), sign, above, this);
        }

        @Override
        public String getDescription() {

            return "Breaks blocks above/below block sign is on.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "Optional block ID",
                    null
            };
            return lines;
        }
    }
}