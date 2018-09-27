package com.sk89q.craftbook.mechanics.ic;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;

public class ICCommands {

    public ICCommands(CraftBookPlugin plugin) {

    }

    @Command (aliases = {"ic","circuit"}, desc = "Information for a specific IC", usage = "<ic> (Further arguments depend on IC)", min = 1)
    public void icCmd(CommandContext args, CommandSender sender) throws CommandException {

        if(ICManager.inst() == null)
            throw new CommandException("ICs are not enabled!");
        if(args.getString(0).equalsIgnoreCase("list")) {

            String list = "";

            for(RegisteredICFactory factory : ICManager.inst().registered.values()) {
                if(factory.getFactory() instanceof CommandIC) {

                    if(list.isEmpty())
                        list = factory.getId();
                    else
                        list = list + ", " + factory.getId();
                }
            }

            sender.sendMessage(ChatColor.YELLOW + "Command IC List: " + list);
        } else {

            RegisteredICFactory factory = ICManager.inst().registered.get(args.getString(0));

            if(factory != null && factory.getFactory() instanceof CommandIC) {
                if(((CommandIC) factory.getFactory()).getMinCommandArgs()+1 > args.argsLength())
                    throw new CommandException();
                ((CommandIC) factory.getFactory()).onICCommand(args, sender);
            }
        }
    }

    @Command(aliases = {"docs"}, desc = "Documentation on CraftBook IC's",
            usage = "<ic>", min = 1, max = 1)
    public void docsCmd(CommandContext args, CommandSender sender) throws CommandException {

        if(ICManager.inst() == null)
            throw new CommandException("ICs are not enabled!");
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        ICDocsParser.generateICDocs(player, args.getString(0));
    }

    @Command(aliases = {"list"}, desc = "List available IC's",
            flags = "p:", usage = "[-p page]", min = 0, max = 0)
    public void listCmd(CommandContext args, CommandSender sender) throws CommandException {

        if(ICManager.inst() == null)
            throw new CommandException("ICs are not enabled!");
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        char[] ar = null;
        try {
            ar = args.getString(1).toCharArray();
        } catch (Exception ignored) {
        }
        String[] lines = ICManager.inst().generateICText(player, null, ar);
        int pages = (lines.length - 1) / 9 + 1;
        int accessedPage;

        try {
            accessedPage = !args.hasFlag('p') ? 0 : args.getFlagInteger('p') - 1;
            if (accessedPage < 0 || accessedPage >= pages) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlagInteger('p') + "\"");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlag('p') + '"');
            return;
        }

        player.sendMessage(ChatColor.BLUE + "  ");
        player.sendMessage(ChatColor.BLUE + "CraftBook ICs (Page " + (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
            player.sendMessage(lines[i]);
        }
    }

    @Command(aliases = {"search"}, desc = "Search available IC's with names",
            flags = "p:", usage = "[-p page] <name>", min = 1, max = 1)
    public void searchCmd(CommandContext args, CommandSender sender) throws CommandException {

        if(ICManager.inst() == null)
            throw new CommandException("ICs are not enabled!");
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        char[] ar = null;
        try {
            ar = args.getString(2).toCharArray();
        } catch (Exception ignored) {
        }
        String[] lines = ICManager.inst().generateICText(player, args.getString(0), ar);
        int pages = (lines.length - 1) / 9 + 1;
        int accessedPage;

        try {
            accessedPage = !args.hasFlag('p') ? 0 : args.getFlagInteger('p') - 1;
            if (accessedPage < 0 || accessedPage >= pages) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlagInteger('p') + '"');
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlag('p') + '"');
            return;
        }

        player.sendMessage(ChatColor.BLUE + "  ");
        player.sendMessage(ChatColor.BLUE + "CraftBook ICs \"" + args.getString(0) + "\" (Page " +
                (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
            player.sendMessage(lines[i]);
        }
    }

    @Command(aliases = {"midis"}, desc = "List MIDI's available for Melody IC",
            flags = "p:", usage = "[-p page]", min = 0, max = 0)
    public void midiListCmd(CommandContext args, CommandSender sender) throws CommandException {

        if(ICManager.inst() == null)
            throw new CommandException("ICs are not enabled!");
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        List<String> lines = new ArrayList<>();

        FilenameFilter fnf = (dir, name) -> name.endsWith("mid") || name.endsWith(".midi");
        for (File f : ICManager.inst().getMidiFolder().listFiles(fnf)) {
            lines.add(f.getName().replace(".midi", "").replace(".mid", ""));
        }
        lines.sort(Comparator.naturalOrder());
        int pages = (lines.size() - 1) / 9 + 1;
        int accessedPage;

        try {
            accessedPage = !args.hasFlag('p') ? 0 : args.getFlagInteger('p') - 1;
            if (accessedPage < 0 || accessedPage >= pages) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlagInteger('p') + "\"");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlag('p') + "\"");
            return;
        }

        player.sendMessage(ChatColor.BLUE + "  ");
        player.sendMessage(ChatColor.BLUE + "CraftBook MIDIs (Page " + (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.size() && i < (accessedPage + 1) * 9; i++) {
            player.sendMessage(ChatColor.GREEN + lines.get(i));
        }
    }

    @Command(aliases = {"fireworks"}, desc = "List Fireworks available for PFD IC",
            flags = "p:", usage = "[-p page]", min = 0, max = 0)
    public void fireworkListCmd(CommandContext args, CommandSender sender) throws CommandException {

        if(ICManager.inst() == null)
            throw new CommandException("ICs are not enabled!");
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        List<String> lines = new ArrayList<>();

        FilenameFilter fnf = (dir, name) -> name.endsWith(".fwk") || name.endsWith(".txt");
        for (File f : ICManager.inst().getFireworkFolder().listFiles(fnf)) {
            lines.add(f.getName().replace(".txt", "").replace(".fwk", ""));
        }
        lines.sort(String::compareTo);
        int pages = (lines.size() - 1) / 9 + 1;
        int accessedPage;

        try {
            accessedPage = !args.hasFlag('p') ? 0 : args.getFlagInteger('p') - 1;
            if (accessedPage < 0 || accessedPage >= pages) {
                player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlagInteger('p') + "\"");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid page \"" + args.getFlag('p') + "\"");
            return;
        }

        player.sendMessage(ChatColor.BLUE + "  ");
        player.sendMessage(ChatColor.BLUE + "CraftBook Firework Displays (Page " + (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.size() && i < (accessedPage + 1) * 9; i++) {
            player.sendMessage(ChatColor.GREEN + lines.get(i));
        }
    }
}
