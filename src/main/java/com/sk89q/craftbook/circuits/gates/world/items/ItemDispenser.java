// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.circuits.gates.world.items;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.worldedit.blocks.BlockType;

public class ItemDispenser extends AbstractIC {

    public ItemDispenser(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemStack item;

    @Override
    public void load() {

        int amount = 1;

        item = ItemUtil.getItem(getLine(2));
        if(item == null)
            item = new ItemStack(1, 1);

        try {
            amount = Math.min(item.getMaxStackSize(), Math.max(1, Integer.parseInt(getSign().getLine(3))));
        } catch (Exception ignored) {
            amount = 1;
        }
        if (amount < 1) amount = 1;

        item.setAmount(amount);
    }

    @Override
    public String getTitle() {

        return "Item Dispenser";
    }

    @Override
    public String getSignTitle() {

        return "ITEM DISPENSER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            if (item.getTypeId() != 36) {
                Location loc = getBackBlock().getRelative(0, 1,
                        0).getLocation().add(0.5, 0.5, 0.5);
                int maxY = 10;

                for (int y = 1; y <= maxY; y++) {
                    if (BlockType.canPassThrough(loc.getBlock().getRelative(0, y, 0).getTypeId())) {

                        ItemStack stack = item.clone();

                        BukkitUtil.toSign(getSign()).getWorld().dropItem(loc.getBlock().getRelative(0, y,
                                0).getLocation(), stack);
                        return;
                    }
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemDispenser(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Spawns in items.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"id:data", "amount"};
            return lines;
        }
    }
}