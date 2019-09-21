package com.sk89q.craftbook.mechanics.variables;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.exceptions.FastCommandException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class VariableCommands {

    private CraftBookPlugin plugin;

    public VariableCommands(CraftBookPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(aliases = "set", desc = "Sets a variable.", max=2, min=2, flags="n:", usage = "<Variable> <Value> -n <Namespace>")
    public void set(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            VariableManager.instance.setVariable(context.getString(0), key, context.getString(1));
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + VariableManager.instance.getVariable(context.getString(0), key));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "define", desc = "Defines a variable.", max=2, min=2, flags="n:", usage = "<Variable> <Value> -n <Namespace>")
    public void define(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(!VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!hasVariablePermission(sender, key, context.getString(0), "define"))
                throw new CommandPermissionsException();
            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");
            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            VariableManager.instance.setVariable(context.getString(0), key, context.getString(1));
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + VariableManager.instance.getVariable(context.getString(0), key));
        } else
            throw new FastCommandException("Existing Variable!");
    }

    @Command(aliases = "get", desc = "Checks a variable.", max=1, min=1, flags="n:", usage = "<Variable> -n <Namespace>")
    public void get(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!hasVariablePermission(sender, key, context.getString(0), "get"))
                throw new CommandPermissionsException();
            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");
            sender.sendMessage(ChatColor.YELLOW + context.getString(0) + ": " + VariableManager.instance.getVariable(context.getString(0), key));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "list", desc = "Lists variables", flags="an:p:", usage = "-p <page> -n <Namespace> -a")
    public void list(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if (context.hasFlag('a'))
            key = null;

        List<String> variablesLines = new ArrayList<>();

        for (Entry<Tuple2<String, String>, String> entry : VariableManager.instance.getVariableStore().entrySet()) {
            if (key != null && !entry.getKey().b.equals(key)) {
                continue;
            }

            String keyName = entry.getKey().a;
            if (key == null) {
                keyName = entry.getKey().b + '|' + keyName;
            }

            variablesLines.add(ChatColor.YELLOW + keyName + ChatColor.WHITE + ": " + ChatColor.GREEN + entry.getValue());
        }

        String[] lines = variablesLines.toArray(new String[variablesLines.size()]);
        int pages = (lines.length - 1) / 9 + 1;
        int accessedPage;

        try {
            accessedPage = !context.hasFlag('p') ? 0 : context.getFlagInteger('p') - 1;
            if (accessedPage < 0 || accessedPage >= pages) {
                sender.sendMessage(ChatColor.RED + "Invalid page \"" + context.getFlagInteger('p') + '"');
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid page \"" + context.getFlag('p') + '"');
            return;
        }

        sender.sendMessage(ChatColor.BLUE + "  ");
        sender.sendMessage(ChatColor.BLUE + "Variables (Page " + (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
            sender.sendMessage(lines[i]);
        }
    }

    @Command(aliases = {"erase","remove","delete","rm"}, desc = "Erase a variable.", max=1, min=1, flags="n:", usage = "<Variable> -n <Namespace>")
    public void erase(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!hasVariablePermission(sender, key, context.getString(0), "erase"))
                throw new CommandPermissionsException();
            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");
            VariableManager.instance.removeVariable(context.getString(0), key);
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Removed variable: " + context.getString(0));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    private static void resetICCache(String variable, String namespace) {

        if(ICManager.inst() != null) {//Make sure IC's are enabled.

            ICManager.getCachedICs().entrySet()
                    .removeIf(ic -> ic.getValue().getSign().hasVariable(namespace + '|' + variable) || ic.getValue().getSign().hasVariable(variable));
        }
    }

    @Command(aliases = "append", desc = "Append to a variable.", max=2, min=2, flags="n:", usage = "<Variable> <Appended Value> -n <Namespace>")
    public void append(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            VariableManager.instance.setVariable(context.getString(0), key, VariableManager.instance.getVariable(context.getString(0), key) + context.getString(1));
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + VariableManager.instance.getVariable(context.getString(0), key));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "prepend", desc = "Prepend to a variable.", max=2, min=2, flags="n:", usage = "<Variable> <Prepended Value> -n <Namespace>")
    public void prepend(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            VariableManager.instance.setVariable(context.getString(0), key, context.getString(1) + VariableManager.instance.getVariable(context.getString(0), key));
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + VariableManager.instance.getVariable(context.getString(0), key));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "toggle", desc = "Toggle a boolean.", max=1, min=1, flags="n:", usage = "<Variable> -n <Namespace>")
    public void toggle(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            String var = VariableManager.instance.getVariable(context.getString(0), key);
            if(var.equalsIgnoreCase("0") || var.equalsIgnoreCase("1"))
                var = var.equalsIgnoreCase("1") ? "0" : "1";
            else if(var.equalsIgnoreCase("true") || var.equalsIgnoreCase("false"))
                var = var.equalsIgnoreCase("true") ? "false" : "true";
            else if(var.equalsIgnoreCase("yes") || var.equalsIgnoreCase("no"))
                var = var.equalsIgnoreCase("yes") ? "no" : "yes";
            else
                throw new FastCommandException("Variable not of boolean type!");
            VariableManager.instance.setVariable(context.getString(0), key, var);
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "add", desc = "Add to a numeric variable.", max=2, min=2, flags="n:", usage = "<Variable> <Added Value> -n <Namespace>")
    public void add(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = VariableManager.instance.getVariable(context.getString(0), key);
            try {

                double f = Double.parseDouble(var);
                f += context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(context.getString(0), key, var);
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "subtract", desc = "Subtract from a numeric variable.", max=2, min=2, flags="n:", usage = "<Variable> <Subtracting Value> -n <Namespace>")
    public void subtract(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = VariableManager.instance.getVariable(context.getString(0), key);
            try {

                double f = Double.parseDouble(var);
                f -= context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(context.getString(0), key, var);
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = {"multiply","multiple"}, desc = "Multiply a numeric variable.", max=2, min=2, flags="n:", usage = "<Variable> <Multiplying Value> -n <Namespace>")
    public void multiple(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = VariableManager.instance.getVariable(context.getString(0), key);
            try {

                double f = Double.parseDouble(var);
                f *= context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(context.getString(0), key, var);
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "divide", desc = "Divide a numeric variable.", max=2, min=2, flags="n:", usage = "<Variable> <Dividing Value> -n <Namespace>")
    public void divide(CommandContext context, CommandSender sender) throws CommandException {

        if (VariableManager.instance == null) {
            sender.sendMessage(ChatColor.RED + "Variables are not enabled!");
            return;
        }

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && sender instanceof Player)
            key = CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId();

        if(context.hasFlag('n'))
            key = context.getFlag('n');

        if(VariableManager.instance.hasVariable(context.getString(0), key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, key, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = VariableManager.instance.getVariable(context.getString(0), key);
            try {

                double f = Double.parseDouble(var);
                if(f == 0)
                    throw new FastCommandException("Can't divide by 0!");
                f /= context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch (RuntimeException e) {
                throw e;
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(context.getString(0), key, var);
            resetICCache(context.getString(0), key);
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    private static void checkModifyPermissions(CommandSender sender, String key, String var) throws CommandException {
        if(!hasVariablePermission(sender, key, var, "modify"))
            throw new CommandPermissionsException();
    }

    /**
     * Checks a players ability to interact with variables.
     * 
     * @param sender The one who is attempting to interact.
     * @param namespace The namespace
     * @param var The variable
     * @param action The action
     * @return true if allowed.
     */
    public static boolean hasVariablePermission(CommandSender sender, String namespace, String var, String action) {

        if(sender instanceof Player && namespace.equalsIgnoreCase(CraftBookPlugin.inst().wrapPlayer((Player) sender).getCraftBookId()))
            if(sender.hasPermission("craftbook.variables." + action + ".self") || sender.hasPermission("craftbook.variables." + action + ".self." + var))
                return true;

        return !(!sender.hasPermission("craftbook.variables." + action + "")
                && !sender.hasPermission("craftbook.variables." + action + '.' + namespace)
                && !sender.hasPermission("craftbook.variables." + action + '.' + namespace + '.' + var));

    }
}