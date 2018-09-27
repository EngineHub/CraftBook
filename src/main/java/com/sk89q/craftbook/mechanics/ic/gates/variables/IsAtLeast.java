package com.sk89q.craftbook.mechanics.ic.gates.variables;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.BukkitCraftBookPlayer;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.variables.VariableCommands;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.RegexUtil;

public class IsAtLeast extends AbstractSelfTriggeredIC {

    public IsAtLeast (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Is At Least";
    }

    @Override
    public String getSignTitle () {
        return "IS AT LEAST";
    }

    String variable;
    double amount;

    @Override
    public void load() {

        try {
            variable = getLine(2);
            amount = Double.parseDouble(getLine(3));
        } catch(Exception ignored) {}
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0)) {
            chip.setOutput(0, isAtLeast());
        }
    }

    @Override
    public void think(ChipState chip) {
        chip.setOutput(0, isAtLeast());
    }

    public boolean isAtLeast() {
        String var,key;
        var = VariableManager.getVariableName(variable);
        key = VariableManager.getNamespace(variable);

        double existing = Double.parseDouble(VariableManager.instance.getVariable(var, key));

        return existing >= amount;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new IsAtLeast(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
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

            return new String[] {"Variable Name", "Amount"};
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
                Double.parseDouble(sign.getLine(3));
            } catch(NumberFormatException e) {
                throw new ICVerificationException("Amount must be a number!");
            }
        }
    }
}