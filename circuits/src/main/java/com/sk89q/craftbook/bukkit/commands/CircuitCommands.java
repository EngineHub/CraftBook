package com.sk89q.craftbook.bukkit.commands;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RegisteredICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;

public class CircuitCommands {

    CircuitsPlugin plugin;

    public CircuitCommands(CircuitsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"ic"},
            desc = "Commands to manage Craftbook IC's"
            )
    public void ic(CommandContext context, CommandSender sender) {
    }

    @Command(
            aliases = {"reloadics"},
            desc = "Reloads the IC config"
            )
    public void reload(CommandContext context, CommandSender sender) {

        plugin.getICConfiguration().reload();
        sender.sendMessage("The IC config has been reloaded.");
    }

    @Command(
            aliases = {"cbcircuits"},
            desc = "Handles the basic Craftbook Circuits commands."
            )
    @NestedCommand(NestedCommands.class)
    public void cbcircuits(CommandContext context, CommandSender sender) {

    }


    public static class NestedCommands {

        private final CircuitsPlugin plugin;

        public NestedCommands(CircuitsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"reload"},
                desc = "Reloads the craftbook circuits config"
                )
        @CommandPermissions("craftbook.circuit.reload")
        public void reload(CommandContext context, CommandSender sender) {

            plugin.getLocalConfiguration().reload();
            sender.sendMessage("Config has been reloaded successfully!");
        }
    }

    @Command(
            aliases = {"icdocs"},
            desc = "Documentation on CraftBook IC's",
            min = 1,
            max = 1
            )
    public void icdocs(CommandContext context, CommandSender sender) {
        if(!(sender instanceof Player)) return;
        Player player = (Player)sender;
        RegisteredICFactory ric = plugin.icManager.registered.get(context.getString(0).toLowerCase());
        if(ric == null) {
            player.sendMessage(ChatColor.RED + "Invalid IC!");
            return;
        }
        try {
            IC ic = ric.getFactory().create(null);
            player.sendMessage(ChatColor.BLUE + ic.getTitle() + " (" + ric.getId() + ") Documentation");
            if(plugin.getLocalConfiguration().enableShorthandIcs && ric.getShorthand() != null) {
                player.sendMessage(ChatColor.YELLOW + "Shorthand: =" + ric.getShorthand());
            }
            player.sendMessage(ChatColor.YELLOW + "Desc: " + ric.getFactory().getDescription());
            if(ric.getFactory().getLineHelp()[0] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 3: " + ric.getFactory().getLineHelp()[0]);
            }
            else {
                player.sendMessage(ChatColor.YELLOW + "Line 3: Nothing.");
            }
            if(ric.getFactory().getLineHelp()[1] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 4: " + ric.getFactory().getLineHelp()[1]);
            }
            else {
                player.sendMessage(ChatColor.YELLOW + "Line 4: Nothing.");
            }
            player.sendMessage(ChatColor.AQUA + "Wiki: " + "http://wiki.sk89q.com/wiki/CraftBook/" + ric.getId().toUpperCase());
        }
        catch(Exception e){}
    }

    @Command(
            aliases = {"listics"},
            desc = "List available IC's",
            min = 0,
            max = 1
            )
    public void listics(CommandContext context, CommandSender sender) {

        if(!(sender instanceof Player)) return;
        Player player = (Player)sender;
        String[] lines = generateICText(player);
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

        player.sendMessage(ChatColor.BLUE + "CraftBook ICs (Page " + (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
            player.sendMessage(lines[i]);
        }
    }

    /**
     * Used for the /listics command.
     *
     * @param p
     *
     * @return
     */
    private String[] generateICText(Player p) {

        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(plugin.icManager.registered.keySet());

        Collections.sort(icNameList);

        ArrayList<String> strings = new ArrayList<String>();
        boolean col = true;
        for (String ic : icNameList) {
            try {
                col = !col;
                RegisteredICFactory ric = plugin.icManager.registered.get(ic);
                IC tic = ric.getFactory().create(null);
                ChatColor colour = col ? ChatColor.YELLOW : ChatColor.GOLD;

                if (ric.getFactory() instanceof RestrictedIC) {
                    if (!p.hasPermission("craftbook.ic.restricted." + ic.toLowerCase())) {
                        colour = col ? ChatColor.RED : ChatColor.DARK_RED;
                    }
                }
                else if (!p.hasPermission("craftbook.ic.safe." + ic.toLowerCase())) {
                    colour = col ? ChatColor.RED : ChatColor.DARK_RED;
                }
                strings.add(colour + tic.getTitle() + " (" + ric.getId() + ")" + ": " + (tic instanceof SelfTriggeredIC ? "ST " : "T ") + (ric.getFactory() instanceof RestrictedIC ? ChatColor.DARK_RED + "R " : ""));
            }
            catch(Exception e){
                if(ic.endsWith("5001") || ic.endsWith("5000")) {
                    //Stuff
                }
                else {
                    Bukkit.getLogger().severe("An error occured generating the docs for IC: " + ic + ". Please report it to Me4502");
                }
            }
        }

        return strings.toArray(new String[0]);
    }
}