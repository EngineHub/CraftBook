package com.sk89q.craftbook.circuits.gates.variables;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.RegexUtil;

public class NumericModifier extends AbstractIC {

    public NumericModifier (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Variable Modifier";
    }

    @Override
    public String getSignTitle () {
        return "VAR MODIFIER";
    }

    Function function;
    String variable;
    double amount;

    @Override
    public void load() {

        try {
            variable = getLine(2);
            function = Function.valueOf(getLine(3).split(":")[0]);
            amount = Double.parseDouble(getLine(3).split(":")[1]);
        } catch(Exception ignored) {}
    }

    @Override
    public void trigger (ChipState chip) {

        if(function == null) {
            chip.setOutput(0, false);
            return;
        }

        try {
            double currentValue = Double.parseDouble(ParsingUtil.parseVariables(variable, null));

            switch(function) {
                case ADD:
                    currentValue += amount;
                    break;
                case DIVIDE:
                    if(amount == 0) {
                        chip.setOutput(0, false);
                        return;
                    }
                    currentValue /= amount;
                    break;
                case MULTIPLY:
                    currentValue *= amount;
                    break;
                case SUBTRACT:
                    currentValue -= amount;
                    break;
                default:
                    break;
            }

            String var = String.valueOf(currentValue);
            if (var.endsWith(".0"))
                var = var.replace(".0", "");

            CraftBookPlugin.inst().setVariable(variable, "global", var);
            chip.setOutput(0, true);
            return;
        } catch(Exception ignored){}
        chip.setOutput(0, false);
    }

    private enum Function {

        ADD,SUBTRACT,MULTIPLY,DIVIDE;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new NumericModifier(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Modifies a variable using the specified function.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Variable Name", "Function:Amount"};
        }

        @Override
        public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {

            String[] parts = RegexUtil.PIPE_PATTERN.split(sign.getLine(2));
            if(parts.length == 1) {
                if(!player.hasPermission("craftbook.variables.use.global"))
                    throw new ICVerificationException("You do not have permissions to use the global variable namespace!");
            } else
                if(!player.hasPermission("craftbook.variables.use." + parts[0]))
                    throw new ICVerificationException("You do not have permissions to use the " + parts[0] + " variable namespace!");
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            try {
                String[] parts = RegexUtil.PIPE_PATTERN.split(sign.getLine(2));
                if(parts.length == 1) {
                    if(!CraftBookPlugin.inst().hasVariable(sign.getLine(2), "global"))
                        throw new ICVerificationException("Unknown Variable!");
                } else
                    if(!CraftBookPlugin.inst().hasVariable(parts[1], parts[0]))
                        throw new ICVerificationException("Unknown Variable!");
                Function.valueOf(sign.getLine(3).split(":")[0]);
                Double.parseDouble(sign.getLine(3).split(":")[1]);
            } catch(NumberFormatException e) {
                throw new ICVerificationException("Amount must be a number!");
            } catch(IllegalArgumentException e) {
                throw new ICVerificationException("Invalid Function!");
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new ICVerificationException("Both Function and Amount must be entered, seperated by a colon (:)!");
            }
        }
    }
}