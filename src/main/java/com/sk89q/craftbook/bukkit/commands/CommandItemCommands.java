package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mech.CommandItems;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;

public class CommandItemCommands {

    public CommandItemCommands(CraftBookPlugin plugin) {
    }

    //private CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(aliases = {"give"}, desc = "Gives the player the item.", usage = "CommandItem Name", min = 1, max = 1)
    public void giveItem(CommandContext context, CommandSender sender) throws CommandException {

        if(!(sender instanceof Player))
            throw new CommandException("Only a player can use this command!");

        if(CommandItems.INSTANCE == null)
            throw new CommandException("CommandItems are not enabled!");

        if(!sender.hasPermission("craftbook.mech.commanditems.give." + context.getString(0)))
            throw new CommandPermissionsException();

        CommandItemDefinition def = CommandItems.INSTANCE.getDefinitionByName(context.getString(0));
        if(def == null)
            throw new CommandException("Invalid CommandItem!");
        if(!((Player) sender).getInventory().addItem(def.getItem()).isEmpty())
            throw new CommandException("Failed to add item to inventory!");
    }
}