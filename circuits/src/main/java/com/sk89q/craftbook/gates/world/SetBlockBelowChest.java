package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

/**
 * @author Me4502
 */
public class SetBlockBelowChest extends AbstractIC {

    public SetBlockBelowChest(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Set Block Below";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK BELOW";
    }

    @Override
    public void trigger(ChipState chip) {

        String sblockdat = getSign().getLine(2).toUpperCase().trim();
        String sblock = sblockdat.split(":")[0];
        String smeta = "";
        if (sblockdat.split(":").length > 1) smeta = sblockdat.split(":")[1];
        String force = getSign().getLine(3).toUpperCase().trim();

        chip.setOutput(0, chip.getInput(0));

        int block = -1;
        BlockType bt = BlockType.lookup(sblock, true);
        if (bt != null) block = bt.getID();

        //FIXME hack for broken WorldEdit <=5.1
        if (block == -1)
            try {
                block = Integer.parseInt(sblock);
            } catch (Exception e) {
                return;
            }

        byte meta = -1;
        try {
            if (!smeta.equalsIgnoreCase("")) meta = Byte.parseByte(smeta);
        } catch (Exception e) {
            return;
        }


        Block body = SignUtil.getBackBlock(getSign().getBlock());

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (force.equals("FORCE") || body.getWorld().getBlockAt(x, y - 1, z).getType() == Material.AIR) {
            if (takeFromChest(x, y + 1, z, block, meta)) {
                body.getWorld().getBlockAt(x, y - 1, z).setTypeId(block);
                if (!(meta == -1)) body.getWorld().getBlockAt(x, y - 1, z).setData(meta);
            }
        }
    }

    public boolean takeFromChest(int x, int y, int z, int id, byte data) {

        boolean ret = false;
        Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        if (bl.getType() == Material.CHEST) {
            Chest c = ((Chest) bl.getState());
            ItemStack[] is = c.getInventory().getContents();
            for (int i = 0; i < is.length; i++) {
                if (is[i] == null) continue;
                if (is[i].getAmount() > 0 && is[i].getTypeId() == id) {
                    if (data != -1)
                        if (!(is[i].getData().getData() == data)) continue;
                    ItemStack stack = is[i];
                    getSign().getWorld().dropItemNaturally(new Location(getSign().getWorld(), x, y, z), stack);
                    if (is[i].getAmount() == 1) is[i] = new ItemStack(0, 0);
                    else is[i].setAmount(is[i].getAmount() - 1);
                    ret = true;
                    break;
                }
            }
            c.getInventory().setContents(is);
        }
        return ret;
    }

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new SetBlockBelowChest(getServer(), sign);
        }
    }
}
