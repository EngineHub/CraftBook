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

package org.enginehub.craftbook.mechanics.ic.gates.variables;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.variables.VariableKey;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;

public class ItemCounter extends AbstractIC {

    public ItemCounter(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {
        return "Item Counter";
    }

    @Override
    public String getSignTitle() {
        return "ITEM COUNTER";
    }

    String variable;
    ItemStack item;

    @Override
    public void load() {

        variable = getLine(2);
        item = ItemSyntax.getItem(getLine(3));
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {

            int amount = 0;

            if (InventoryUtil.doesBlockHaveInventory(getBackBlock().getRelative(0, 1, 0))) {
                InventoryHolder chest = (InventoryHolder) getBackBlock().getRelative(0, 1, 0).getState();
                for (ItemStack stack : chest.getInventory().getContents()) {
                    if (!ItemUtil.isStackValid(stack)) continue;
                    if (item == null || ItemUtil.areItemsIdentical(stack, item)) {
                        amount += stack.getAmount();
                    }
                }
            }

            chip.setOutput(0, amount > 0);

            try {
                VariableKey variableKey = VariableKey.fromString(variable, null);

                double existing = Double.parseDouble(VariableManager.instance.getVariable(variableKey));

                String val = String.valueOf(existing + amount);
                if (val.endsWith(".0"))
                    val = StringUtils.replace(val, ".0", "");

                VariableManager.instance.setVariable(variableKey, val);
            } catch (VariableException e) {
                CraftBook.logger.error("Failed to tick IC at " + getBackBlock().getLocation(), e);
            }
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemCounter(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''VAR200''' IC searches a chest and counts the amounts of all items that match the last line of the sign. ",
                "The counted amount is then added to the variable listed on the 3rd line.",
            };
        }

        @Override
        public String getShortDescription() {

            return "Adds to a variable the amount of items of a type counted.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "High if found item"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Variable Name", "ItemSyntax" };
        }

        @Override
        public void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
            try {
                VariableKey variableKey = VariableKey.fromString(sign.getLine(2), player);
                if (variableKey != null && !variableKey.hasPermission(player, "use")) {
                    throw new ICVerificationException("You do not have permissions to use " + variableKey.toString() + "");
                }
            } catch (VariableException e) {
                throw new ICVerificationException("Can't use variable", e);
            }
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            try {
                VariableKey variableKey = VariableKey.fromString(sign.getLine(2), null);
                if (variableKey == null || !VariableManager.instance.hasVariable(variableKey)) {
                    throw new ICVerificationException("Unknown Variable!");
                }
            } catch (VariableException e) {
                throw new ICVerificationException("Can't use variable", e);
            }
        }
    }
}