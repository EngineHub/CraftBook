package com.sk89q.craftbook.bukkit.commands;
import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.ReportWriter;
import com.sk89q.craftbook.bukkit.Updater;
import com.sk89q.craftbook.bukkit.Updater.UpdateResult;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.PastebinPoster;
import com.sk89q.craftbook.util.PastebinPoster.PasteCallback;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
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

        @Command(aliases = "update", desc = "Updates CraftBook.", flags = "c", max = 0)
        @CommandPermissions("craftbook.update")
        public void update(CommandContext context, CommandSender sender) {

            if(!CraftBookPlugin.inst().getConfiguration().updateNotifier) {
                sender.sendMessage("Functionality disabled!");
                return;
            }

            if (context.hasFlag('c')) {

                CraftBookPlugin.inst().checkForUpdates();
            } else {

                if(!CraftBookPlugin.inst().updateAvailable) {
                    sender.sendMessage("No updates are available!");
                    return;
                }
                Updater updater = new Updater(CraftBookPlugin.inst(), "CraftBook", CraftBookPlugin.inst().getFile(), Updater.UpdateType.DEFAULT, true);
                if(updater.getResult() == UpdateResult.NO_UPDATE)
                    sender.sendMessage("No updates are available!");
            }
        }

        @Command(aliases = "about", desc = "Gives info about craftbook.")
        public void about(CommandContext context, CommandSender sender) {

            String ver = CraftBookPlugin.inst().getDescription().getVersion();
            if(CraftBookPlugin.inst().versionConverter.inverse().get(ver) != null)
                ver = CraftBookPlugin.inst().versionConverter.inverse().get(ver) + " (" + CraftBookPlugin.inst().getDescription().getVersion() + ")";
            sender.sendMessage(ChatColor.YELLOW + "CraftBook version " + ver);
            sender.sendMessage(ChatColor.YELLOW + "Founded by sk89q, and currently developed by me4502 & Dark_Arc");
        }

        @Command(aliases = {"report"}, desc = "Writes a report on CraftBook", flags = "p", max = 0)
        @CommandPermissions({"craftbook.report"})
        public void report(CommandContext args, final CommandSender sender) throws CommandPermissionsException {

            File dest = new File(CraftBookPlugin.inst().getDataFolder(), "report.txt");
            ReportWriter report = new ReportWriter(CraftBookPlugin.inst());

            try {
                report.write(dest);
                sender.sendMessage(ChatColor.YELLOW + "CraftBook report written to "
                        + dest.getAbsolutePath());
            } catch (IOException e) {
                throw new CommandException("Failed to write report: " + e.getMessage());
            }

            if (args.hasFlag('p')) {
                CraftBookPlugin.inst().checkPermission(sender, "craftbook.report.pastebin");

                sender.sendMessage(ChatColor.YELLOW + "Now uploading to Pastebin...");
                PastebinPoster.paste(report.toString(), new PasteCallback() {

                    @Override
                    public void handleSuccess(String url) {
                        // Hope we don't have a thread safety issue here
                        sender.sendMessage(ChatColor.YELLOW + "CraftBook report (1 hour): " + url);
                    }

                    @Override
                    public void handleError(String err) {
                        // Hope we don't have a thread safety issue here
                        sender.sendMessage(ChatColor.YELLOW + "CraftBook report pastebin error: " + err);
                    }
                });
            }

        }
    }
}
