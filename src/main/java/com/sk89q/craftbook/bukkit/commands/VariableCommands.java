package com.sk89q.craftbook.bukkit.commands;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.exceptions.FastCommandException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.BlockWorldVector;

public class VariableCommands {

    CraftBookPlugin plugin;

    public VariableCommands(CraftBookPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(aliases = "set", desc = "Sets a variable.", max=2, min=2)
    public void set(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            plugin.setVariable(context.getString(0), "global", context.getString(1));
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + plugin.getVariable(context.getString(0), "global"));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "define", desc = "Defines a variable.", max=2, min=2)
    @CommandPermissions("craftbook.variables.define")
    public void define(CommandContext context, CommandSender sender) throws CommandException {

        if(!plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");
            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            plugin.setVariable(context.getString(0), "global", context.getString(1));
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + plugin.getVariable(context.getString(0), "global"));
        } else
            throw new FastCommandException("Existing Variable!");
    }

    @Command(aliases = "get", desc = "Checks a variable.", max=1, min=1)
    @CommandPermissions("craftbook.variables.get")
    public void get(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");
            sender.sendMessage(ChatColor.YELLOW + context.getString(0) + ": " + plugin.getVariable(context.getString(0), "global"));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "erase", desc = "Erase a variable.", max=1, min=1)
    @CommandPermissions("craftbook.variables.erase")
    public void erase(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");
            plugin.removeVariable(context.getString(0), "global");
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Removed variable: " + context.getString(0));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    public void resetICCache(String variable) {

        if(CircuitCore.inst() != null)
            if(CircuitCore.inst().getIcManager() != null) {//Make sure IC's are enabled.

                Iterator<Entry<BlockWorldVector, IC>> iterator = ICManager.getCachedICs().entrySet().iterator();
                while(iterator.hasNext()) {
                    Entry<BlockWorldVector, IC> ic = iterator.next();
                    if(ic.getValue().getSign().hasVariable(variable))
                        iterator.remove();
                }
            }
    }

    @Command(aliases = "append", desc = "Append to a variable.", max=2, min=2)
    public void append(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            plugin.setVariable(context.getString(0), "global", plugin.getVariable(context.getString(0), "global") + context.getString(1));
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + plugin.getVariable(context.getString(0), "global"));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "prepend", desc = "Prepend to a variable.", max=2, min=2)
    public void prepend(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");
            plugin.setVariable(context.getString(0), "global", context.getString(1) + plugin.getVariable(context.getString(0), "global"));
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + plugin.getVariable(context.getString(0), "global"));
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "toggle", desc = "Toggle a boolean.", max=1, min=1)
    public void toggle(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            String var = plugin.getVariable(context.getString(0), "global");
            if(var.equalsIgnoreCase("0") || var.equalsIgnoreCase("1"))
                var = var.equalsIgnoreCase("1") ? "0" : "1";
            else if(var.equalsIgnoreCase("true") || var.equalsIgnoreCase("false"))
                var = var.equalsIgnoreCase("true") ? "false" : "true";
            else if(var.equalsIgnoreCase("yes") || var.equalsIgnoreCase("no"))
                var = var.equalsIgnoreCase("yes") ? "no" : "yes";
            else
                throw new FastCommandException("Variable not of boolean type!");
            plugin.setVariable(context.getString(0), "global", var);
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "add", desc = "Add to a numeric variable.", max=2, min=2)
    public void add(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = plugin.getVariable(context.getString(0), "global");
            try {

                double f = Double.parseDouble(var);
                f += context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            plugin.setVariable(context.getString(0), "global", var);
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "subtract", desc = "Subtract from a numeric variable.", max=2, min=2)
    public void subtract(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = plugin.getVariable(context.getString(0), "global");
            try {

                double f = Double.parseDouble(var);
                f -= context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            plugin.setVariable(context.getString(0), "global", var);
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = {"multiply","multiple"}, desc = "Multiply a numeric variable.", max=2, min=2)
    public void multiple(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = plugin.getVariable(context.getString(0), "global");
            try {

                double f = Double.parseDouble(var);
                f *= context.getDouble(1);
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new FastCommandException("Variable not of numeric type!");
            }
            plugin.setVariable(context.getString(0), "global", var);
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    @Command(aliases = "divide", desc = "Divide a numeric variable.", max=2, min=2)
    public void divide(CommandContext context, CommandSender sender) throws CommandException {

        if(plugin.hasVariable(context.getString(0), "global")) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(context.getString(0)).find())
                throw new FastCommandException("Invalid Variable Name!");

            checkModifyPermissions(sender, context.getString(0));

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(context.getString(1)).find())
                throw new FastCommandException("Invalid Variable Value!");

            String var = plugin.getVariable(context.getString(0), "global");
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
            plugin.setVariable(context.getString(0), "global", var);
            resetICCache(context.getString(0));
            sender.sendMessage(ChatColor.YELLOW + "Variable is now: " + var);
        } else
            throw new FastCommandException("Unknown Variable!");
    }

    public void checkModifyPermissions(CommandSender sender, String key) throws CommandException {

        if(!sender.hasPermission("craftbook.variables.modify") && !sender.hasPermission("craftbook.variables.modify." + key))
            throw new CommandPermissionsException();
    }
}