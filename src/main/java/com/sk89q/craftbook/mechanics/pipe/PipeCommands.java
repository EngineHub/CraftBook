package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class PipeCommands {

    public PipeCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"networks", "network"}, desc = "Opens a menu of recently active pipe networks")
    @CommandPermissions("craftbook.pipes.networks")
    public void networks(CommandContext context, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player player))
            throw new CommandException("This command can only be used in-game.");
        PipeNetworks.get().open(player);
    }
}
