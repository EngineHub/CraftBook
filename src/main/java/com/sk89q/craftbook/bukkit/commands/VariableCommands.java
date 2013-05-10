package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class VariableCommands {

    CraftBookPlugin plugin;

    public VariableCommands(CraftBookPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(aliases = "set", desc = "Sets a variable.", max=2, min=2)
    @CommandPermissions("craftbook.variables.set")
    public void set(CommandContext context, CommandSender sender) {

        if(plugin.variableStore.containsKey(context.getString(0))) {

            if(!RegexUtil.VARIABLE_PATTERN.matcher(context.getString(0)).find()) {
                sender.sendMessage("Invalid Variable Name!");
                return;
            }
            if(!RegexUtil.VARIABLE_PATTERN.matcher(context.getString(1)).find()) {
                sender.sendMessage("Invalid Variable Value!");
                return;
            }
            plugin.variableStore.put(context.getString(0), context.getString(1));
        } else
            sender.sendMessage("Unknown Variable!");
    }

    @Command(aliases = "define", desc = "Defines a variable.", max=2, min=2)
    @CommandPermissions("craftbook.variables.define")
    public void define(CommandContext context, CommandSender sender) {

        if(!plugin.variableStore.containsKey(context.getString(0))) {

            if(!RegexUtil.VARIABLE_PATTERN.matcher(context.getString(0)).find()) {
                sender.sendMessage("Invalid Variable Name!");
                return;
            }
            if(!RegexUtil.VARIABLE_PATTERN.matcher(context.getString(1)).find()) {
                sender.sendMessage("Invalid Variable Value!");
                return;
            }
            plugin.variableStore.put(context.getString(0), context.getString(1));
        } else
            sender.sendMessage("Existing Variable!");
    }

    @Command(aliases = "get", desc = "Checks a variable.", max=1, min=1)
    @CommandPermissions("craftbook.variables.get")
    public void get(CommandContext context, CommandSender sender) {

        if(plugin.variableStore.containsKey(context.getString(0))) {

            if(!RegexUtil.VARIABLE_PATTERN.matcher(context.getString(0)).find()) {
                sender.sendMessage("Invalid Variable Name!");
                return;
            }
            sender.sendMessage(context.getString(0) + ": " + plugin.variableStore.get(context.getString(0)));
        } else
            sender.sendMessage("Unknown Variable!");
    }

    @Command(aliases = "erase", desc = "Erase a variable.", max=1, min=1)
    @CommandPermissions("craftbook.variables.erase")
    public void erase(CommandContext context, CommandSender sender) {

        if(plugin.variableStore.containsKey(context.getString(0))) {

            if(!RegexUtil.VARIABLE_PATTERN.matcher(context.getString(0)).find()) {
                sender.sendMessage("Invalid Variable Name!");
                return;
            }
            plugin.variableStore.remove(context.getString(0));
        } else
            sender.sendMessage("Unknown Variable!");
    }
}