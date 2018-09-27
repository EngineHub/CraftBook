package com.sk89q.craftbook.mechanics.ic.gates.variables;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.BukkitCraftBookPlayer;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.variables.VariableCommands;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.InventoryUtil;
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

            int amount = 0;

            if(InventoryUtil.doesBlockHaveInventory(getBackBlock().getRelative(0, 1, 0))) {
                InventoryHolder chest = (InventoryHolder) getBackBlock().getRelative(0, 1, 0).getState();
                for(ItemStack stack : chest.getInventory().getContents()) {
                    if(!ItemUtil.isStackValid(stack)) continue;
                    if(item == null || ItemUtil.areItemsIdentical(stack, item)) {
                        amount += stack.getAmount();
                    }
                }
            }

            chip.setOutput(0, amount > 0);

            String var,key;
            var = VariableManager.getVariableName(variable);
            key = VariableManager.getNamespace(variable);

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
        public String[] getLongDescription() {

            return new String[]{
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

            return new String[] {"Variable Name", "ItemSyntax"};
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