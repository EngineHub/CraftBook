package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class BlockBreaker extends AbstractSelfTriggeredIC {

    boolean above;

    public BlockBreaker(Server server, ChangedSign block, boolean above, ICFactory factory) {

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

    @Override
    public void think(ChipState state) {

        state.setOutput(0, breakBlock());
    }

    Block broken, chest;

    int id;
    byte data;

    @Override
    public void load() {

        try {
            String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(2));
            id = Integer.parseInt(split[0]);
            data = Byte.parseByte(split[1]);
        } catch (Exception ignored) {
        }
    }

    public boolean breakBlock() {

        if (chest == null || broken == null) {

            Block bl = getBackBlock();

            if (above) {
                chest = bl.getRelative(0, 1, 0);
                broken = bl.getRelative(0, -1, 0);
            } else {
                chest = bl.getRelative(0, -1, 0);
                broken = bl.getRelative(0, 1, 0);
            }
        }

        boolean hasChest = false;
        if (chest != null && chest.getTypeId() == BlockID.CHEST) {
            hasChest = true;
        }
        if (broken == null || broken.getTypeId() == 0 || broken.getTypeId() == BlockID.BEDROCK || broken.getTypeId()
                == BlockID.PISTON_MOVING_PIECE)
            return false;

        if (id > 0 && id != broken.getTypeId()) return false;

        if (data > 0 && data != broken.getData()) return false;

        for (ItemStack blockstack : broken.getDrops()) {

            BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
            Block pipe = getBackBlock().getRelative(back);

            if(Pipes.Factory.setupPipes(pipe, getBackBlock(), Arrays.asList(blockstack)) != null)
                continue;

            if (hasChest) {
                Chest c = (Chest) chest.getState();
                HashMap<Integer, ItemStack> overflow = c.getInventory().addItem(blockstack);
                if (overflow.isEmpty()) continue;
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

        BukkitUtil.toSign(getSign()).getWorld().dropItem(BlockUtil.getBlockCentre(BukkitUtil.toSign(getSign())
                .getBlock()), item);
    }

    public static class Factory extends AbstractICFactory {

        boolean above;

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockBreaker(getServer(), sign, above, this);
        }

        @Override
        public String getShortDescription() {

            return "Breaks blocks above/below block sign is on.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Block ID{:Data}", null};
        }
    }
}