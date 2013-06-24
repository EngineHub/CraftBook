package com.sk89q.craftbook.circuits.gates.world.items;

import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * @author Me4502
 */
public class ContainerDispenser extends AbstractSelfTriggeredIC {

    public ContainerDispenser(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemStack item;
    int amount;

    @Override
    public void load() {

        try {
            amount = Integer.parseInt(getSign().getLine(2));
        } catch (Exception e) {
            amount = 1;
        }

        item = ItemSyntax.getItem(getLine(3));
        if(item != null)
            item.setAmount(amount);
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

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, dispense());
    }

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean dispense() {

        Block b = getBackBlock();

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
                    if(item == null || ItemUtil.areItemsIdentical(it, item)) {
                        stack = it;
                        inv = c.getInventory();
                        break;
                    }
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
                    if(item == null || ItemUtil.areItemsIdentical(it, item)) {
                        stack = it;
                        inv = c.getInventory();
                        break;
                    }
                }
            }
        } else if (bl.getTypeId() == BlockID.DISPENSER) {
            Dispenser c = (Dispenser) bl.getState();
            for (ItemStack it : c.getInventory().getContents()) {
                if (ItemUtil.isStackValid(it)) {
                    if(item == null || ItemUtil.areItemsIdentical(it, item)) {
                        stack = it;
                        inv = c.getInventory();
                        break;
                    }
                }
            }
        }

        return !(stack == null || inv == null) && dispenseItem(inv, stack);
    }

    public boolean dispenseItem(Inventory inv, ItemStack old) {

        ItemStack item = old.clone();
        item.setAmount(amount);
        if (inv == null) return false;
        HashMap<Integer, ItemStack> over = inv.removeItem(item.clone());
        if (over.isEmpty()) {
            BukkitUtil.toSign(getSign()).getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation(), item.clone());
            return true;
        } else {
            for (ItemStack it : over.values()) {

                if (item.getAmount() - it.getAmount() < 1) continue;
                BukkitUtil
                .toSign(getSign())
                .getWorld()
                .dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation(), new ItemStack(it.getTypeId(), item.getAmount() - it.getAmount(), it.getDurability()));
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
        public String getShortDescription() {

            return "Dispenses items out of containers.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"amount to dispense", null};
        }
    }
}
