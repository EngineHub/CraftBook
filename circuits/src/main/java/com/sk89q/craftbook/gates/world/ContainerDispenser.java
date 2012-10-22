package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.*;
import org.bukkit.inventory.ItemStack;

/**
 * @author Me4502
 */
public class ContainerDispenser extends AbstractIC {

    private int amount = 1;

    public ContainerDispenser(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
        try {
            amount = Integer.parseInt(getSign().getLine(2));
        } catch (Exception ignored) {
            // use default
        }
    }

    @Override
    public String getTitle() {

        return "Container Dispenser";
    }

    @Override
    public String getSignTitle() {

        return "CONTAINER DISPENSER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, dispense());
        }
    }

    Block bl;

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
        bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        ItemStack stack = null;
        if (bl.getType() == Material.CHEST) {
            Chest c = (Chest) bl.getState();
            for (ItemStack it : c.getInventory().getContents())
                if (ItemUtil.isStackValid(it)) {
                    stack = it;
                }
        } else if (bl.getType() == Material.FURNACE || bl.getType() == Material.BURNING_FURNACE) {
            Furnace c = (Furnace) bl.getState();
            stack = c.getInventory().getResult();
        } else if (bl.getType() == Material.BREWING_STAND) {
            BrewingStand c = (BrewingStand) bl.getState();
            for (ItemStack it : c.getInventory().getContents())
                if (ItemUtil.isStackValid(it)) {
                    if (ItemUtil.areItemsIdentical(it, c.getInventory().getIngredient())) {
                        continue;
                    }
                    stack = it;
                }
        } else if (bl.getType() == Material.DISPENSER) {
            Dispenser c = (Dispenser) bl.getState();
            for (ItemStack it : c.getInventory().getContents())
                if (ItemUtil.isStackValid(it)) {
                    stack = it;
                }
        }

        if (stack == null) return false;
        dispenseItem(stack);
        return true;
    }

    public ItemStack dispenseItem(ItemStack item) {

        int curA = item.getAmount();
        int a = amount;
        if (curA < a) {
            a = curA;
        }
        ItemStack stack = new ItemStack(item.getTypeId(), a, item.getData().getData());
        getSign().getWorld().dropItem(BlockUtil.getBlockCentre(getSign().getBlock()), stack);
        item.setAmount(curA - a);
        if (item.getAmount() <= 1) {
            item = null;
        }
        if (bl.getType() == Material.FURNACE || bl.getType() == Material.BURNING_FURNACE) {
            ((Furnace) bl.getState()).getInventory().setResult(item);
        }
        return item;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new ContainerDispenser(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Dispenses items out of containers.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    null,
                    null
            };
            return lines;
        }
    }
}
