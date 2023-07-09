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

package org.enginehub.craftbook.mechanics.ic.gates.world.sensors;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SearchArea;

public class PlayerInventorySensor extends AbstractSelfTriggeredIC {

    public PlayerInventorySensor(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, isDetected());
    }

    @Override
    public String getTitle() {
        return "Player Inventory Sensor";
    }

    @Override
    public String getSignTitle() {
        return "PLAYER INV SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0))
            chip.setOutput(0, isDetected());
    }

    SearchArea area;
    ItemStack item;
    int minPlayers;
    int slot;
    boolean inHand;

    @Override
    public void load() {

        area = SearchArea.createArea(getSign().getBlock(), getLine(2));

        String[] parts = RegexUtil.EQUALS_PATTERN.split(getLine(3));
        item = ItemUtil.makeItemValid(ItemSyntax.getItem(parts[0]));
        if (parts.length > 1) {

            String[] data = RegexUtil.COLON_PATTERN.split(parts[1]);
            try {
                minPlayers = Integer.parseInt(data[0]);
                inHand = Boolean.parseBoolean(data[1]);
                try {
                    slot = Integer.parseInt(data[2]);
                } catch (Exception e) {
                    slot = -1;
                }
            } catch (Exception e) {
                if (minPlayers <= 0)
                    minPlayers = 1;
                inHand = false;
            }
        } else {
            minPlayers = 1;
            inHand = false;
            slot = -1;
        }
    }

    public boolean isDetected() {

        int players = 0;

        for (Player e : area.getPlayersInArea()) {
            if (e == null || !e.isValid())
                continue;

            if (testPlayer(e))
                players += 1;

            if (players >= minPlayers)
                return true;
        }

        return false;
    }

    public boolean testPlayer(Player e) {

        if (slot == -1 && !inHand)
            return InventoryUtil.doesInventoryContain(e.getInventory(), false, item);
        else if (inHand) { //Eclipse messes with indentation without these {'s
            return (e.getInventory().getItemInMainHand() != null && ItemUtil.areItemsIdentical(e.getInventory().getItemInMainHand(), item) && e.getInventory().getItemInMainHand().getAmount() >= item.getAmount())
                || (e.getInventory().getItemInOffHand() != null && ItemUtil.areItemsIdentical(e.getInventory().getItemInOffHand(), item) && e.getInventory().getItemInOffHand().getAmount() >= item.getAmount());
        } else if (slot > -1) {
            return e.getInventory().getItem(slot) != null && ItemUtil.areItemsIdentical(e.getInventory().getItem(slot), item) && e.getInventory().getItem(slot).getAmount() >= item.getAmount();
        }

        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlayerInventorySensor(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Detects if a certain number of players have an item in their inventory.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {
                "+oSearchArea",
                "item=minPlayers:inHand:slot"
            };
        }
    }
}