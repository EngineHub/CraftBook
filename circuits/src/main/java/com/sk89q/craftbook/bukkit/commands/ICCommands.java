package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;

public class ICCommands {

    private final CircuitsPlugin plugin;

    public ICCommands(CircuitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(
            aliases = {"ic"},
            desc = "Commands to manage Craftbook IC's"
            )
    public void ic(CommandContext context, CommandSender sender) {

    }


    @Command(
            aliases = {"listics"},
            desc = "List available IC's"
            )
    public void listics(CommandContext context, CommandSender sender) {
        sender.sendMessage(plugin.getICList());
    }

}
