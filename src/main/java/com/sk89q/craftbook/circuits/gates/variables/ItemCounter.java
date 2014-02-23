package com.sk89q.craftbook.circuits.gates.variables;

import org.apache.tools.ant.util.StringUtils;
import org.bukkit.Server;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.commands.VariableCommands;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.common.variables.VariableManager;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;

public class ItemCounter extends AbstractIC {

    public ItemCounter (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Item Counter";
    }

    @Override
    public String getSignTitle () {
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
    public void trigger (ChipState chip) {

        if(chip.getInput(0)) {
            BlockState state = getBackBlock().getRelative(0, 1, 0).getState();

            int amount = 0;

            if(state instanceof InventoryHolder) {
                InventoryHolder chest = (InventoryHolder) state;
                for(ItemStack stack : chest.getInventory().getContents()) {
                    if(!ItemUtil.isStackValid(stack)) continue;
                    if(item == null || ItemUtil.areItemsIdentical(stack, item)) {
                        amount += stack.getAmount();
                    }
                }
            }

            chip.setOutput(0, amount > 0);

            String var,key;
            var = VariableManager.instance.getVariableName(variable);
            key = VariableManager.instance.getNamespace(variable);

            double existing = Double.parseDouble(VariableManager.instance.getVariable(var, key));

            String val = String.valueOf(existing + amount);
            if (val.endsWith(".0"))
                val = StringUtils.replace(val, ".0", "");

            VariableManager.instance.setVariable(var, key, val);
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
        public String getShortDescription() {

            return "Adds to a variable the amount of items of a type counted.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Variable Name", "ItemSyntax"};
        }

        @Override
        public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {

            String[] parts = RegexUtil.PIPE_PATTERN.split(sign.getLine(2));
            if(parts.length == 1) {
                if(!VariableCommands.hasVariablePermission(((BukkitPlayer) player).getPlayer(), "global", parts[0], "use"))
                    throw new ICVerificationException("You do not have permissions to use the global variable namespace!");
            } else
                if(!VariableCommands.hasVariablePermission(((BukkitPlayer) player).getPlayer(), parts[0], parts[1], "use"))
                    throw new ICVerificationException("You do not have permissions to use the " + parts[0] + " variable namespace!");
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            String[] parts = RegexUtil.PIPE_PATTERN.split(sign.getLine(2));
            if(parts.length == 1) {
                if(!VariableManager.instance.hasVariable(sign.getLine(2), "global"))
                    throw new ICVerificationException("Unknown Variable!");
            } else
                if(!VariableManager.instance.hasVariable(parts[1], parts[0]))
                    throw new ICVerificationException("Unknown Variable!");
        }
    }
}