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

package org.enginehub.craftbook.mechanics.ic.gates.variables;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.variables.VariableKey;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;

public class IsAtLeast extends AbstractSelfTriggeredIC {

    public IsAtLeast(Server server, BukkitChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {
        return "Is At Least";
    }

    @Override
    public String getSignTitle() {
        return "IS AT LEAST";
    }

    String variable;
    double amount;

    @Override
    public void load() {

        try {
            variable = getLine(2);
            amount = Double.parseDouble(getLine(3));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isAtLeast());
        }
    }

    @Override
    public void think(ChipState chip) {
        chip.setOutput(0, isAtLeast());
    }

    public boolean isAtLeast() {
        try {
            VariableKey variableKey = VariableKey.fromString(variable, null);
            double existing = Double.parseDouble(VariableManager.instance.getVariable(variableKey));

            return existing >= amount;
        } catch (VariableException e) {
            CraftBook.LOGGER.error("Failed to tick IC at " + getBackBlock().getLocation(), e);
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new IsAtLeast(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''VAR170''' IC checks a numerical variable against an amount listed on the sign.",
                "If the variable on the sign has a value greater than that listed on the sign, the IC will output high."

            };
        }

        @Override
        public String getShortDescription() {

            return "Checks if a variable is at least...";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "High if variable is at least"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Variable Name", "Amount" };
        }

        @Override
        public void checkPlayer(BukkitChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
            try {
                VariableKey variableKey = VariableKey.fromString(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)), player);
                if (variableKey != null && !variableKey.hasPermission(player, "use")) {
                    throw new ICVerificationException("You do not have permissions to use " + variableKey.toString() + "");
                }
            } catch (VariableException e) {
                throw new ICVerificationException("Can't use this variable", e);
            }
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {
            try {
                VariableKey variableKey = VariableKey.fromString(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)), null);
                if (variableKey == null || !VariableManager.instance.hasVariable(variableKey)) {
                    throw new ICVerificationException("Unknown Variable!");
                }
                Double.parseDouble(PlainTextComponentSerializer.plainText().serialize(sign.getLine(3)));
            } catch (NumberFormatException e) {
                throw new ICVerificationException("Amount must be a number!");
            } catch (VariableException e) {
                throw new ICVerificationException("Can't use this variable", e);
            }
        }
    }
}
