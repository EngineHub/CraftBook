package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;

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
        Block bl = CraftBookBukkitUtil.toSign(getSign()).getBlock().getWorld().getBlockAt(x, y, z);
        ItemStack stack = null;
        Inventory inv = null;
        if (bl.getType() == Material.CHEST) {
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
        } else if (bl.getType() == Material.FURNACE) {
            Furnace c = (Furnace) bl.getState();
            stack = c.getInventory().getResult();
            inv = c.getInventory();
        } else if (bl.getType() == Material.BREWING_STAND) {
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
        } else if (bl.getType() == Material.DISPENSER) {
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

            BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
            Block pipe = getBackBlock().getRelative(back);

            PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(Collections.singletonList(item.clone())), getBackBlock());
            Bukkit.getPluginManager().callEvent(event);

            if(!event.isValid())
                return true;

            for(ItemStack stack : event.getItems())
                CraftBookBukkitUtil.toSign(getSign()).getWorld().dropItemNaturally(CraftBookBukkitUtil.toSign(getSign()).getLocation(), stack);
            return true;
        } else {

            BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
            Block pipe = getBackBlock().getRelative(back);

            PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(over.values()), getBackBlock());
            Bukkit.getPluginManager().callEvent(event);

            if(!event.isValid())
                return true;

            for (ItemStack it : event.getItems()) {

                if (item.getAmount() - it.getAmount() < 1) continue;
                CraftBookBukkitUtil.toSign(getSign()).getWorld().dropItemNaturally(
                        CraftBookBukkitUtil.toSign(getSign()).getLocation(), new ItemStack(it.getType(), item.getAmount() - it.getAmount(), it.getDurability()));
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
