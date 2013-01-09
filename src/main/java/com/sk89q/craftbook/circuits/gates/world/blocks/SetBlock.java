package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

public abstract class SetBlock extends AbstractIC {

    public SetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    int block;
    String force;
    byte meta;

    @Override
    public void load() {

        String[] splitBlockData = RegexUtil.COLON_PATTERN.split(getSign().getLine(2).toUpperCase().trim(), 2);
        String strBlock = splitBlockData[0];
        String strMeta = "";
        if (splitBlockData.length > 1) {
            strMeta = splitBlockData[1];
        }
        force = getSign().getLine(3).toUpperCase().trim();

        try {
            block = Integer.parseInt(strBlock);
        } catch (Exception e) {
            try {
                block = BlockType.lookup(strBlock).getID();
            } catch (Exception ignored) {
            }
        }

        try {
            if (!strMeta.isEmpty()) {
                meta = Byte.parseByte(strMeta);
            } else meta = -1;
        } catch (Exception ignored) {
            meta = -1;
        }
    }

    public void onTrigger() {

        if (BlockType.fromID(block) == null || block >= 256) {
            return;
        }

        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        doSet(body, block, meta, force.equals("FORCE"));
    }

    @Override
    public void trigger(ChipState chip) {

        chip.setOutput(0, chip.getInput(0));

        onTrigger();
    }

    protected abstract void doSet(Block block, int id, byte meta, boolean force);

    public boolean takeFromChest(Block bl, int id, byte data) {

        if (bl.getTypeId() != BlockID.CHEST) {
            return false;
        }
        BlockState state = bl.getState();
        if (!(state instanceof Chest)) {
            return false;
        }
        Chest c = (Chest) state;
        ItemStack[] is = c.getInventory().getContents();
        for (int i = 0; i < is.length; i++) {
            final ItemStack stack = is[i];
            if (stack == null) {
                continue;
            }
            if (stack.getAmount() > 0 && stack.getTypeId() == id) {
                if (data != -1 && stack.getData().getData() != data) {
                    continue;
                }
                if (stack.getAmount() == 1) {
                    is[i] = new ItemStack(0, 0);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                c.getInventory().setContents(is);
                c.update();
                return true;
            }
        }
        return false;
    }
}
