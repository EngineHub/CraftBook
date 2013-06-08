package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;

/**
 * @author Silthus
 */
public class MechanismCommands {

    public MechanismCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"area", "togglearea"}, desc = "Commands to manage Craftbook Areas")
    @NestedCommand(AreaCommands.class)
    public void area(CommandContext context, CommandSender sender) {

    }

    @Command(aliases = {"recp", "recps"}, desc = "Commands to manage Craftbook Custom Recipes")
    @NestedCommand(RecipeCommands.class)
    public void recipe(CommandContext context, CommandSender sender) {

    }

    @Command(aliases = {"comitems", "commanditems", "citems", "commanditem"}, desc = "Commands to manage Craftbook Command Items")
    @NestedCommand(CommandItemCommands.class)
    public void commandItems(CommandContext context, CommandSender sender) {
    }

    @Command(aliases = {"cauldron"}, desc = "Commands to manage the Craftbook Cauldron")
    @NestedCommand(CauldronCommands.class)
    public void cauldron(CommandContext context, CommandSender sender) {

    }
}
