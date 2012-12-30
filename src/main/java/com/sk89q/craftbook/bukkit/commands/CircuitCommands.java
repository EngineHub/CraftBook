package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CircuitCommands {

    private static CircuitCore circuitCore = CircuitCore.inst();

    public CircuitCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"ic"}, desc = "Commands to manage Craftbook IC's")
    @NestedCommand(Commands.class)
    public void icCmd(CommandContext context, CommandSender sender) {

    }

    public class Commands {

        public Commands(CraftBookPlugin plugin) {

        }

        @Command(aliases = {"docs"}, desc = "Documentation on CraftBook IC's", min = 1, max = 1)
        public void docsCmd(CommandContext context, CommandSender sender) {

            if (!(sender instanceof Player)) return;
            Player player = (Player) sender;
            circuitCore.generateICDocs(player, context.getString(0));
        }

        @Command(aliases = {"list"}, desc = "List available IC's", min = 0, max = 2)
        public void listCmd(CommandContext context, CommandSender sender) {

            if (!(sender instanceof Player)) return;
            Player player = (Player) sender;
            char[] ar = null;
            try {
                ar = context.getString(1).toCharArray();
            } catch (Exception ignored) {
            }
            String[] lines = circuitCore.generateICText(player, null, ar);
            int pages = (lines.length - 1) / 9 + 1;
            int accessedPage;

            try {
                accessedPage = context.argsLength() < 1 ? 0 : context.getInteger(0) - 1;
                if (accessedPage < 0 || accessedPage >= pages) {
                    player.sendMessage(ChatColor.RED + "Invalid page \"" + context.getInteger(0) + "\"");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + context.getInteger(0) + "\"");
                return;
            }

            player.sendMessage(ChatColor.BLUE + "  ");
            player.sendMessage(ChatColor.BLUE + "CraftBook ICs (Page " + (accessedPage + 1) + " of " + pages + "):");

            for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
                player.sendMessage(lines[i]);
            }
        }

        @Command(aliases = {"search"}, desc = "Search available IC's with names", min = 1, max = 3)
        public void searchCmd(CommandContext context, CommandSender sender) {

            if (!(sender instanceof Player)) return;
            Player player = (Player) sender;
            char[] ar = null;
            try {
                ar = context.getString(2).toCharArray();
            } catch (Exception ignored) {
            }
            String[] lines = circuitCore.generateICText(player, context.getString(0), ar);
            int pages = (lines.length - 1) / 9 + 1;
            int accessedPage;

            try {
                accessedPage = context.argsLength() < 2 ? 0 : context.getInteger(1) - 1;
                if (accessedPage < 0 || accessedPage >= pages) {
                    player.sendMessage(ChatColor.RED + "Invalid page \"" + context.getInteger(1) + "\"");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + context.getInteger(1) + "\"");
                return;
            }

            player.sendMessage(ChatColor.BLUE + "  ");
            player.sendMessage(ChatColor.BLUE + "CraftBook ICs \"" + context.getString(0) + "\" (Page " +
                    (accessedPage +
                            1) + " of " + pages + "):");

            for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
                player.sendMessage(lines[i]);
            }
        }

        // TODO Move MIDI
        @Command(aliases = {"midi"}, desc = "List MIDI's available for Melody IC", min = 0, max = 1)
        public void midiListCmd(CommandContext context, CommandSender sender) {

            if (!(sender instanceof Player)) return;
            Player player = (Player) sender;
            List<String> lines = new ArrayList<String>();

            FilenameFilter fnf = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    return name.endsWith("mid") || name.endsWith(".midi");
                }
            };
            for (File f : circuitCore.getMidiFolder().listFiles(fnf)) {
                lines.add(f.getName().replace(".midi", "").replace(".mid", ""));
            }
            Collections.sort(lines, new Comparator<String>() {

                @Override
                public int compare(String f1, String f2) {

                    return f1.compareTo(f2);
                }
            });
            int pages = (lines.size() - 1) / 9 + 1;
            int accessedPage;

            try {
                accessedPage = context.argsLength() < 1 ? 0 : context.getInteger(0) - 1;
                if (accessedPage < 0 || accessedPage >= pages) {
                    player.sendMessage(ChatColor.RED + "Invalid page \"" + context.getInteger(0) + "\"");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + context.getInteger(0) + "\"");
                return;
            }

            player.sendMessage(ChatColor.BLUE + "  ");
            player.sendMessage(ChatColor.BLUE + "CraftBook MIDIs (Page " + (accessedPage + 1) + " of " + pages + "):");

            for (int i = accessedPage * 9; i < lines.size() && i < (accessedPage + 1) * 9; i++) {
                player.sendMessage(ChatColor.GREEN + lines.get(i));
            }
        }
    }
}