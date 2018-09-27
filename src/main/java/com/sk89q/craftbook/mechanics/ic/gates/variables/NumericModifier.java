package com.sk89q.craftbook.mechanics.ic.gates.variables;

import com.sk89q.craftbook.CraftBookPlayer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitCraftBookPlayer;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.variables.VariableCommands;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
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

    MathFunction function;
    String variable;
    double amount;

    @Override
    public void load() {

        try {
            variable = getLine(2);
            function = MathFunction.parseFunction(getLine(3).split(":")[0]);
            amount = Double.parseDouble(getLine(3).split(":")[1]);
        } catch(Exception ignored) {}
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0)) {
            if(function == null) {
                chip.setOutput(0, false);
                return;
            }

            try {
                String var,key;
                var = VariableManager.getVariableName(variable);
                key = VariableManager.getNamespace(variable);

                double currentValue = Double.parseDouble(VariableManager.instance.getVariable(var,key));

                currentValue = function.parseNumber(currentValue, amount);

                String val = String.valueOf(currentValue);
                if (val.endsWith(".0"))
                    val = StringUtils.replace(val, ".0", "");

                VariableManager.instance.setVariable(var, key, val);
                chip.setOutput(0, true);
                return;
            } catch(NumberFormatException ignored){}
        }

        chip.setOutput(0, false);
    }

    public enum MathFunction {

        ADD("+"),SUBTRACT("-"),MULTIPLY("*","x"),DIVIDE("/"),MOD("%");

        String[] mini;

        MathFunction(String ... mini) {
            this.mini = mini;
        }

        public static MathFunction parseFunction(String text) {

            for(MathFunction func : values()) {
                if(func.name().equalsIgnoreCase(text))
                    return func;
                for(String min : func.mini)
                    if(min.equalsIgnoreCase(text))
                        return func;
            }

            return null;
        }

        public double parseNumber(double initial, double amount) {
            switch(this) {
                case ADD:
                    initial += amount;
                    break;
                case DIVIDE:
                    if(amount == 0) {
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
        public IC create(ChangedSign sign) {

            return new NumericModifier(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Modifies a variable using the specified function.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
                    "The '''VAR100''' IC allows for the modification of numerical variables using common binary operations (Known an Functions).",
                    "",
                    "== Functions ==" ,
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

            return new String[] {"Variable Name", "Function:Amount"};
        }

        @Override
        public void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException {

            String[] parts = RegexUtil.PIPE_PATTERN.split(sign.getLine(2));
            if(parts.length == 1) {
                if(!VariableCommands.hasVariablePermission(((BukkitCraftBookPlayer) player).getPlayer(), "global", parts[0], "use"))
                    throw new ICVerificationException("You do not have permissions to use the global variable namespace!");
            } else
                if(!VariableCommands.hasVariablePermission(((BukkitCraftBookPlayer) player).getPlayer(), parts[0], parts[1], "use"))
                    throw new ICVerificationException("You do not have permissions to use the " + parts[0] + " variable namespace!");
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            try {
                String[] parts = RegexUtil.PIPE_PATTERN.split(sign.getLine(2));
                if(parts.length == 1) {
                    if(!VariableManager.instance.hasVariable(sign.getLine(2), "global"))
                        throw new ICVerificationException("Unknown Variable!");
                } else
                    if(!VariableManager.instance.hasVariable(parts[1], parts[0]))
                        throw new ICVerificationException("Unknown Variable!");
                Validate.notNull(MathFunction.parseFunction(sign.getLine(3).split(":")[0]));
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