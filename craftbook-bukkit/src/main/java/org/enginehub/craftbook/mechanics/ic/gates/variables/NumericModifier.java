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

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
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

public class NumericModifier extends AbstractIC {

    public NumericModifier(Server server, BukkitChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {
        return "Variable Modifier";
    }

    @Override
    public String getSignTitle() {
        return "VAR MODIFIER";
    }

    MathFunction function;
    String variable;
    double amount;

    @Override
    public void load() {

        try {
            variable = getLine(2);
            function = MathFunction.parseFunction(getLine(3).split(":")[0]);
            amount = Double.parseDouble(getLine(3).split(":")[1]);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            if (function == null) {
                chip.setOutput(0, false);
                return;
            }

            try {
                VariableKey variableKey = VariableKey.fromString(variable, null);

                double currentValue = Double.parseDouble(VariableManager.instance.getVariable(variableKey));

                currentValue = function.parseNumber(currentValue, amount);

                String val = String.valueOf(currentValue);
                if (val.endsWith(".0"))
                    val = val.replace(".0", "");

                VariableManager.instance.setVariable(variableKey, val);
                chip.setOutput(0, true);
                return;
            } catch (NumberFormatException | VariableException ignored) {
            }
        }

        chip.setOutput(0, false);
    }

    public enum MathFunction {

        ADD("+"), SUBTRACT("-"), MULTIPLY("*", "x"), DIVIDE("/"), MOD("%");

        String[] mini;

        MathFunction(String... mini) {
            this.mini = mini;
        }

        public static MathFunction parseFunction(String text) {

            for (MathFunction func : values()) {
                if (func.name().equalsIgnoreCase(text))
                    return func;
                for (String min : func.mini)
                    if (min.equalsIgnoreCase(text))
                        return func;
            }

            return null;
        }

        public double parseNumber(double initial, double amount) {
            switch (this) {
                case ADD:
                    initial += amount;
                    break;
                case DIVIDE:
                    if (amount == 0) {
                        return initial;
                    }
                    initial /= amount;
                    break;
                case MULTIPLY:
                    initial *= amount;
                    break;
                case SUBTRACT:
                    initial -= amount;
                    break;
                case MOD:
                    initial %= amount;
                    break;
                default:
                    break;
            }

            return initial;
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new NumericModifier(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Modifies a variable using the specified function.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''VAR100''' IC allows for the modification of numerical variables using common binary operations (Known an Functions).",
                "",
                "== Functions ==",
                "{| class=\"wiki-table sortable\"",
                "! Name",
                "! Symbol",
                "! Function",
                "|-",
                "| Add || + || Adds the inputted value to the variable.",
                "|-",
                "| Subtract || - || Subtracts the inputted value from the variable.",
                "|-",
                "| Multiply || * OR x || Multiplies the inputted value by the variable.",
                "|-",
                "| Divide || / || Divides the inputted value by the variable.",
                "|-",
                "| Mod || % || Performs modulo by the inputted value on the variable.",
                "|}"
            };
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "High on success"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Variable Name", "Function:Amount" };
        }

        @Override
        public void checkPlayer(BukkitChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
            VariableKey variableKey;
            try {
                String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
                variableKey = VariableKey.fromString(line2, player);
                if (variableKey != null && !variableKey.hasPermission(player, "use")) {
                    throw new ICVerificationException("You do not have permissions to use " + variableKey + "!");
                }
            } catch (VariableException e) {
                throw new ICVerificationException(e.getMessage(), e);
            }
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {
            try {
                String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
                VariableKey variableKey = VariableKey.fromString(line2, null);
                if (variableKey == null || !VariableManager.instance.hasVariable(variableKey)) {
                    throw new ICVerificationException("Unknown Variable!");
                }
                String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
                Preconditions.checkNotNull(MathFunction.parseFunction(line3.split(":")[0]));
                Double.parseDouble(line3.split(":")[1]);
            } catch (NumberFormatException e) {
                throw new ICVerificationException("Amount must be a number!");
            } catch (IllegalArgumentException e) {
                throw new ICVerificationException("Invalid Function!");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ICVerificationException("Both Function and Amount must be entered, seperated by a colon (:)!");
            } catch (VariableException e) {
                throw new ICVerificationException(e.getMessage(), e);
            }
        }
    }
}