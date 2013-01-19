package com.sk89q.craftbook.bukkit.commands;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;

/**
 * Author: Turtle9598
 */
public class TopLevelCommands {

    public TopLevelCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"craftbook", "cb"}, desc = "CraftBook Plugin commands")
    @NestedCommand(Commands.class)
    public void craftBookCmds(CommandContext context, CommandSender sender) {

    }

    public static class Commands {

        public Commands(CraftBookPlugin plugin) {

        }

        @Command(aliases = "reload", desc = "Reloads the CraftBook Common config")
        @CommandPermissions("craftbook.reload")
        public void reload(CommandContext context, CommandSender sender) {

            try {
                CraftBookPlugin.inst().reloadConfiguration();
            } catch (Throwable e) {
                BukkitUtil.printStacktrace(e);
                sender.sendMessage("An error occured while reloading the CraftBook config.");
                return;
            }
            sender.sendMessage("The CraftBook config has been reloaded.");
        }

        @Command(aliases = "about", desc = "Gives info about craftbook.")
        public void about(CommandContext context, CommandSender sender) {

            sender.sendMessage(ChatColor.YELLOW + "CraftBook version " + CraftBookPlugin.inst().getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Founded by sk89q, and currently developed by me4502 & Dark_Arc");
        }
    }
}
