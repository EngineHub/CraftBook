package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Me4502
 */
public class ContainerDispenser extends AbstractIC {

    private int amount = 1;

    public ContainerDispenser(Server server, Sign sign) {

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
            for (ItemStack it : c.getInventory().getContents()) {
                if(ItemUtil.isStackValid(it))
                    stack = it;
            }
        }
        else if (bl.getType() == Material.FURNACE || bl.getType() == Material.BURNING_FURNACE) {
            Furnace c = (Furnace) bl.getState();
            stack = c.getInventory().getResult();
        }
        else if (bl.getType() == Material.BREWING_STAND) {
            BrewingStand c = (BrewingStand) bl.getState();
            for (ItemStack it : c.getInventory().getContents()) {
                if(ItemUtil.isStackValid(it)) {
                    if(ItemUtil.areItemsIdentical(it,c.getInventory().getIngredient()))
                        continue;
                    stack = it;
                }
            }
        }
        else if (bl.getType() == Material.DISPENSER) {
            Dispenser c = (Dispenser) bl.getState();
            for (ItemStack it : c.getInventory().getContents()) {
                if(ItemUtil.isStackValid(it))
                    stack = it;
            }
        }

        if(stack == null) return false;
        dispenseItem(stack);
        return true;
    }

    public ItemStack dispenseItem(ItemStack item) {
        int curA = item.getAmount();
        int a = amount;
        if(curA < a)
            a = curA;
        ItemStack stack = new ItemStack(item.getTypeId(), a, item.getData().getData());
        getSign().getWorld().dropItemNaturally(new Location(getSign().getWorld(), getSign().getX(),
                getSign().getY(), getSign().getZ()), stack);
        item.setAmount(curA - a);
        if(item.getAmount() <= 1) {
            item = null;
        }
        if(bl.getType() == Material.FURNACE || bl.getType() == Material.BURNING_FURNACE)
            ((Furnace)bl.getState()).getInventory().setResult(item);
        return item;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new ContainerDispenser(getServer(), sign);
        }
    }
}
