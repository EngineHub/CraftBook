package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public abstract class SetBlock extends AbstractIC {

    private static final Pattern DATA_SEPARATOR = Pattern.compile(":", Pattern.LITERAL);

    public SetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void trigger(ChipState chip) {

        String[] splitBlockData = DATA_SEPARATOR.split(getSign().getLine(2).toUpperCase().trim(), 2);
        String strBlock = splitBlockData[0];
        String strMeta = "";
        if (splitBlockData.length > 1) {
            strMeta = splitBlockData[1];
        }
        String force = getSign().getLine(3).toUpperCase().trim();

        chip.setOutput(0, chip.getInput(0));

        int block;

        try {
            block = Integer.parseInt(strBlock);
        } catch (Exception e) {
            try {
                block = Material.getMaterial(strBlock).getId();
            }
            catch(Exception ee) {
                return;
            }
        }

        if (Material.getMaterial(block) == null || block >= 256) {
            return;
        }

        byte meta = -1;
        try {
            if (!strMeta.isEmpty()) {
                meta = Byte.parseByte(strMeta);
            }
        } catch (Exception e) {
            return;
        }


        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        doSet(body, block, meta, force.equals("FORCE"));
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
