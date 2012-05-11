package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;

public class ChestCollector extends AbstractIC {

    protected boolean risingEdge;

    public ChestCollector(Server server, Sign sign, boolean risingEdge) {

        super(server, sign);
        this.risingEdge = risingEdge;
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

        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            chip.setOutput(0, collect());
        }
    }

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean collect() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        if (bl.getType() == Material.CHEST) {
            World w = getSign().getBlock().getWorld();
            for (Item item : w.getEntitiesByClass(Item.class)) {
                int ix = item.getLocation().getBlockX();
                int iy = item.getLocation().getBlockY();
                int iz = item.getLocation().getBlockZ();
                if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {
                    if (((Chest) bl.getState()).getInventory().firstEmpty() != -1) {
                        int id = -1;
                        int exid = -1;
                        try {
                            id = Integer.parseInt(getSign().getLine(2));
                        } catch (Exception e) {
                        }
                        try {
                            exid = Integer.parseInt(getSign().getLine(3));
                        } catch (Exception e) {
                        }
                        if (exid != -1) {
                            if (exid == item.getItemStack().getTypeId())
                                continue;
                        }

                        if (id != -1) {
                            if (id != item.getItemStack().getTypeId())
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

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {

            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {

            return new ChestCollector(getServer(), sign, risingEdge);
        }
    }
}
