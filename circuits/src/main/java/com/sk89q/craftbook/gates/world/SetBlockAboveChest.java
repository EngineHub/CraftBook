package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * @author Me4502
 */
public class SetBlockAboveChest extends SetBlockAbove {

    public SetBlockAboveChest(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Above";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK ABOVE";
    }

    @Override
    public void trigger(ChipState chip) {

        String sblockdat = getSign().getLine(2).toUpperCase().trim();
        String sblock = sblockdat.split(":")[0];
        String smeta = "";
        if (sblockdat.split(":").length > 1) {
            smeta = sblockdat.split(":")[1];
        }
        String force = getSign().getLine(3).toUpperCase().trim();

        chip.setOutput(0, chip.getInput(0));

        int block;

        try {
            block = Integer.parseInt(sblock);
        } catch (Exception e) {
            try {
                block = Material.getMaterial(sblock).getId();
            }
            catch(Exception ee) {
                return;
            }
        }

        byte meta = -1;
        try {
            if (!smeta.equalsIgnoreCase("")) {
                meta = Byte.parseByte(smeta);
            }
        } catch (Exception e) {
            return;
        }


        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (force.equals("FORCE") || body.getWorld().getBlockAt(x, y + 1, z).getTypeId() == BlockID.AIR)
            if (takeFromChest(x, y - 1, z, block, meta)) {
                body.getWorld().getBlockAt(x, y + 1, z).setTypeId(block);
                if (!(meta == -1)) {
                    body.getWorld().getBlockAt(x, y + 1, z).setData(meta);
                }
            }
    }

    public boolean takeFromChest(int x, int y, int z, int id, byte data) {

        boolean ret = false;
        Block bl = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(x, y, z);
        if (bl.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) bl.getState();
            ItemStack[] is = c.getInventory().getContents();
            for (short i = 0; i < is.length; i++) {
                ItemStack stack = is[i];
                if (stack == null) {
                    continue;
                }
                if (stack.getAmount() > 0 && stack.getTypeId() == id) {
                    if (data != -1 && !(stack.getData().getData() == data)) {
                        continue;
                    }
                    if (stack.getAmount() == 1) {
                        is[i] = new ItemStack(0, 0);
                    } else {
                        stack.setAmount(stack.getAmount() - 1);
                    }
                    ret = true;
                    break;
                }
            }
            c.getInventory().setContents(is);
        }
        return ret;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBlockAboveChest(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Sets above block from below chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "id:data",
                    "forced or not"
            };
            return lines;
        }
    }
}
