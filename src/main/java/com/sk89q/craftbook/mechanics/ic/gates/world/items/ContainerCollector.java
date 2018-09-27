package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Me4502
 */
public class ContainerCollector extends AbstractSelfTriggeredIC {

    public ContainerCollector(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Container Collector";
    }

    @Override
    public String getSignTitle() {

        return "CONTAINER COLLECT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, scanForItems());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, scanForItems());
    }

    ItemStack doWant, doNotWant;

    Block chest;

    @Override
    public void load() {

        doWant = ItemSyntax.getItem(getLine(2));
        doNotWant = ItemSyntax.getItem(getLine(3));
        chest = getBackBlock().getRelative(0, 1, 0);
    }

    protected boolean scanForItems() {

        boolean collected = false;
        for (Item item : ItemUtil.getItemsAtBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock()))
            if(item.isValid() && !item.isDead())
                if(collectItem(item))
                    collected = true;

        return collected;
    }

    public boolean collectItem(Item item) {

        ItemStack stack = item.getItemStack();

        if(!ItemUtil.isStackValid(stack))
            return false;

        // Check to see if it matches either test stack, if not stop
        if (doWant != null && !ItemUtil.areItemsIdentical(doWant, stack))
            return false;

        if (doNotWant != null && ItemUtil.areItemsIdentical(doNotWant, stack))
            return false;

        BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        Block pipe = getBackBlock().getRelative(back);

        PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(Collections.singletonList(stack)), getBackBlock());
        Bukkit.getPluginManager().callEvent(event);

        if(event.getItems().isEmpty()) {
            item.remove();
            return true;
        }

        if(!InventoryUtil.doesBlockHaveInventory(chest))
            return false;

        // Add the items to a container, and destroy them.
        List<ItemStack> leftovers = InventoryUtil.addItemsToInventory((InventoryHolder)chest.getState(), stack);
        if(leftovers.isEmpty()) {
            item.remove();
            return true;
        } else {
            if(ItemUtil.areItemsIdentical(leftovers.get(0), stack) && leftovers.get(0).getAmount() != stack.getAmount()) {
                if(!ItemUtil.isStackValid(leftovers.get(0)))
                    item.remove();
                else
                    item.setItemStack(leftovers.get(0));
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

            return new ContainerCollector(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Collects items into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"included id:data", "excluded id:data"};
        }
    }
}