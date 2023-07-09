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
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.mechanics.pipe.PipeRequestEvent;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.SignUtil;

import java.util.ArrayList;
import java.util.Collections;

public class ContainerStocker extends AbstractSelfTriggeredIC {

    public ContainerStocker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemStack item;
    Location offset;

    @Override
    public void load() {

        if (getLine(3).isEmpty())
            offset = getBackBlock().getRelative(0, 1, 0).getLocation();
        else
            offset = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        item = ItemSyntax.getItem(getLine(2));
    }

    @Override
    public String getTitle() {

        return "Container Stocker";
    }

    @Override
    public String getSignTitle() {

        return "STOCKER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, stock());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, stock());
    }

    public boolean stock() {

        if (InventoryUtil.doesBlockHaveInventory(offset.getBlock())) {

            BlockFace back = SignUtil.getBack(getSign().getBlock());
            Block pipe = getBackBlock().getRelative(back);

            PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(Collections.singletonList(item.clone())), getBackBlock());
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isValid())
                return false;

            InventoryHolder c = (InventoryHolder) offset.getBlock().getState();
            for (ItemStack stack : event.getItems())
                if (c.getInventory().addItem(stack).isEmpty()) {
                    //((BlockState) c).update();
                    return true;
                }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerStocker(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Adds item into container at specified offset.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "item id:data", "x:y:z offset" };
        }
    }
}