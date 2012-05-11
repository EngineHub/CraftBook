package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class ChestDispenser extends AbstractIC {

    protected boolean risingEdge;

    public ChestDispenser(Server server, Sign sign, boolean risingEdge) {

        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {

        return "Chest Dispenser";
    }

    @Override
    public String getSignTitle() {

        return "CHEST DISPENSER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            chip.setOutput(0, dispense());
        }
    }

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean dispense() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        if (bl.getType() == Material.CHEST) {
            Chest c = ((Chest) bl.getState());
            ItemStack[] is = c.getInventory().getContents();
            for (int i = 0; i < is.length; i++) {
                if (is[i] == null) continue;
                if (is[i].getAmount() > 0 && is[i].getTypeId() > 0) {
                    ItemStack stack = is[i];
                    getSign().getWorld().dropItemNaturally(new Location(getSign().getWorld(), x, y, z), stack);
                    is[i] = new ItemStack(0, 0);
                    break;
                }
            }
            c.getInventory().setContents(is);
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

            return new ChestDispenser(getServer(), sign, risingEdge);
        }
    }
}
