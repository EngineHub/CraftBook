package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;
import org.bukkit.command.CommandSender;

public class CircuitCommands {

    CircuitsPlugin plugin;

    public CircuitCommands(CircuitsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"ic"},
            desc = "Commands to manage Craftbook IC's"
    )
    @NestedCommand(ICCommands.class)
    public void ic(CommandContext context, CommandSender sender) {

    }


    @Command(
            aliases = {"listics"},
            desc = "List available IC's"
    )
    @NestedCommand(ICCommands.class)
    public void listics(CommandContext context, CommandSender sender) {

    }
}