/*
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

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.PipeInputIC;
import org.enginehub.craftbook.mechanics.pipe.PipePutEvent;
import org.enginehub.craftbook.mechanics.pipe.PipeRequestEvent;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sorter extends AbstractSelfTriggeredIC implements PipeInputIC {

    public Sorter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Block chestBlock;
    boolean inverted;
    boolean ignoreDurability;
    boolean ignoreEnchants;
    boolean ignoreMeta;

    @Override
    public void load() {

        chestBlock = getBackBlock().getRelative(0, 1, 0);
        inverted = getLine(2).equalsIgnoreCase("invert");

        for (String line4 : RegexUtil.PIPE_PATTERN.split(getLine(3))) {
            if (line4.equalsIgnoreCase("!D"))
                ignoreDurability = true;
            if (line4.equalsIgnoreCase("!E"))
                ignoreEnchants = true;
            if (line4.equalsIgnoreCase("!M"))
                ignoreMeta = true;
        }
    }

    @Override
    public String getTitle() {

        return "Sorter";
    }

    @Override
    public String getSignTitle() {

        return "SORTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, sort());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, sort());
    }

    public boolean sort() {

        boolean returnValue = false;

        for (Item item : ItemUtil.getItemsAtBlock(getSign().getBlock())) {
            if (sortItemStack(item.getItemStack())) {
                item.remove();
                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean sortItemStack(final ItemStack item) {

        BlockFace back = SignUtil.getBack(getSign().getBlock());
        Block b;

        if (isInAboveContainer(item) ^ inverted)
            b = SignUtil.getRightBlock(getSign().getBlock()).getRelative(back);
        else
            b = SignUtil.getLeftBlock(getSign().getBlock()).getRelative(back);

        PipeRequestEvent event = new PipeRequestEvent(b, new ArrayList<>(Collections.singletonList(item)), getBackBlock());
        Bukkit.getPluginManager().callEvent(event);

        for (ItemStack it : event.getItems())
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), it);

        return true;
    }

    public boolean isInAboveContainer(ItemStack item) {
        ItemStack itemClone = item.clone();
        itemClone.setAmount(1);
        return InventoryUtil.doesBlockHaveInventory(chestBlock) && InventoryUtil.doesInventoryContain(((InventoryHolder) chestBlock.getState()).getInventory(), true, ignoreDurability, ignoreMeta, ignoreEnchants, itemClone);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Sorter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Sorts items and spits out left/right depending on above chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "invert - to invert output sides", null };
        }
    }

    @Override
    public void onPipeTransfer(PipePutEvent event) {

        List<ItemStack> leftovers = new ArrayList<>();

        for (ItemStack item : event.getItems())
            if (ItemUtil.isStackValid(item))
                if (!sortItemStack(item))
                    leftovers.add(item);

        event.setItems(leftovers);
    }
}