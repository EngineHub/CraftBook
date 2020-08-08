/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.items;

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

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.pipe.PipeRequestEvent;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.SignUtil;

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