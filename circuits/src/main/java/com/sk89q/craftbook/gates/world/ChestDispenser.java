package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
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
public class ChestDispenser extends AbstractIC {

    private int amount = 1;

    public ChestDispenser(Server server, Sign sign) {

        super(server, sign);
        try {
            amount = Integer.parseInt(getSign().getLine(2));
        } catch (Exception ignored) {
            // use default
            sign.setLine(2, amount + "");
            sign.update();
        }
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

        if (chip.getInput(0)) {
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
            for (ItemStack i1 : is) {
                if (ItemUtil.isStackValid(i1)) {
                    int curA = i1.getAmount();
                    ItemStack stack = new ItemStack(i1.getTypeId(), i1.getAmount(), i1.getData().getData());
                    stack.setAmount(1);
                    getSign().getWorld().dropItemNaturally(new Location(getSign().getWorld(), getSign().getX(),
                            getSign().getY(), getSign().getZ()), stack);
                    i1.setAmount(curA - amount);
                    break;
                }
            }
            c.getInventory().setContents(is);
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new ChestDispenser(getServer(), sign);
        }
    }
}
