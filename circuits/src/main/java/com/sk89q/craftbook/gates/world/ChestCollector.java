package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 * @author Me4502
 */
public class ChestCollector extends AbstractIC {

    public ChestCollector(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Chest Collector";
    }

    @Override
    public String getSignTitle() {

        return "CHEST COLLECT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, collect());
    }

    protected boolean collect() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        if (bl.getType() == Material.CHEST) {
            for (Entity en : getSign().getChunk().getEntities()) {
                if (!(en instanceof Item)) continue;
                Item item = (Item) en;
                int ix = item.getLocation().getBlockX();
                int iy = item.getLocation().getBlockY();
                int iz = item.getLocation().getBlockZ();
                if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {
                    if (((Chest) bl.getState()).getInventory().firstEmpty() != -1) {
                        ItemStack id = new ItemStack(0);
                        ItemStack ex = new ItemStack(0);
                        try {
                            if (getSign().getLine(2).contains(":")) {
                                id.setTypeId(Integer.parseInt(getSign().getLine(2).split(":")[0]));
                                id.setDurability((short) Integer.parseInt(getSign().getLine(2).split(":")[1]));
                            } else
                                id.setTypeId(Integer.parseInt(getSign().getLine(2)));
                        } catch (Exception ignored) {
                        }
                        try {
                            if (getSign().getLine(3).contains(":")) {
                                ex.setTypeId(Integer.parseInt(getSign().getLine(3).split(":")[0]));
                                ex.setDurability((short) Integer.parseInt(getSign().getLine(3).split(":")[1]));
                            } else
                                ex.setTypeId(Integer.parseInt(getSign().getLine(3)));
                        } catch (Exception ignored) {
                        }

                        if (ex.getTypeId() != 0) {
                            if (ItemUtil.areItemsIdentical(ex, item.getItemStack()))
                                continue;
                        }

                        if (id.getTypeId() != 0) {
                            if (!ItemUtil.areItemsIdentical(id, item.getItemStack()))
                                continue;
                        }

                        ((Chest) bl.getState()).getInventory().addItem(item.getItemStack());
                        item.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new ChestCollector(getServer(), sign);
        }
    }
}
