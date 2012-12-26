package com.sk89q.craftbook.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Server;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * @author Me4502
 */
public class ContainerDispenser extends AbstractIC {

    public ContainerDispenser(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    int amount;

    @Override
    public void load() {

        try {
            amount = Integer.parseInt(getSign().getLine(2));
        } catch (Exception e) {
            amount = 1;
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

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean dispense() {

        Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = BukkitUtil.toSign(getSign()).getBlock().getWorld().getBlockAt(x, y, z);
        ItemStack stack = null;
        Inventory inv = null;
        if (bl.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) bl.getState();
            for (ItemStack it : c.getInventory().getContents()) {
                if (ItemUtil.isStackValid(it)) {
                    stack = it;
                    inv = c.getInventory();
                }
            }
        } else if (bl.getTypeId() == BlockID.FURNACE || bl.getTypeId() == BlockID.BURNING_FURNACE) {
            Furnace c = (Furnace) bl.getState();
            stack = c.getInventory().getResult();
            inv = c.getInventory();
        } else if (bl.getTypeId() == BlockID.BREWING_STAND) {
            BrewingStand c = (BrewingStand) bl.getState();
            for (ItemStack it : c.getInventory().getContents()) {
                if (ItemUtil.isStackValid(it)) {
                    if (ItemUtil.areItemsIdentical(it, c.getInventory().getIngredient())) {
                        continue;
                    }
                    stack = it;
                    inv = c.getInventory();
                }
            }
        } else if (bl.getTypeId() == BlockID.DISPENSER) {
            Dispenser c = (Dispenser) bl.getState();
            for (ItemStack it : c.getInventory().getContents()) {
                if (ItemUtil.isStackValid(it)) {
                    stack = it;
                    inv = c.getInventory();
                }
            }
        }

        return !(stack == null || inv == null) && dispenseItem(inv, stack);
    }

    public boolean dispenseItem(Inventory inv, ItemStack item) {

        if (inv == null) return false;
        HashMap<Integer, ItemStack> over = inv.removeItem(new ItemStack(item.getTypeId(), amount,
                item.getDurability()));
        if (over.isEmpty()) {
            BukkitUtil.toSign(getSign()).getWorld()
                    .dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation(), new ItemStack(item.getTypeId(),
                            amount, item.getDurability()));
            return true;
        } else {
            for (ItemStack it : over.values()) {

                if (amount - it.getAmount() < 1) continue;
                BukkitUtil
                        .toSign(getSign())
                        .getWorld()
                        .dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation(),
                                new ItemStack(it.getTypeId(), amount - it.getAmount(), it.getDurability()));
                return true;
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerDispenser(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Dispenses items out of containers.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"amount to remove", null};
            return lines;
        }
    }
}
