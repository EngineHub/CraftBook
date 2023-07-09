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

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.ItemUtil;

public class ItemFan extends AbstractSelfTriggeredIC {

    public ItemFan(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    double force;

    @Override
    public void load() {

        try {
            force = Double.parseDouble(getLine(2));
        } catch (Exception ignored) {
            force = 1;
        }
    }

    @Override
    public String getTitle() {

        return "Item Fan";
    }

    @Override
    public String getSignTitle() {

        return "ITEM FAN";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, push());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, push());
    }

    public boolean push() {

        boolean returnValue = false;

        Block aboveBlock = getBackBlock().getRelative(0, 1, 0);

        for (Item item : ItemUtil.getItemsAtBlock(aboveBlock)) {
            item.teleport(item.getLocation().add(0, force, 0));
            returnValue = true;
        }

        return returnValue;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemFan(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Gently pushes items upwards.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "force (default 1)", null };
        }
    }
}