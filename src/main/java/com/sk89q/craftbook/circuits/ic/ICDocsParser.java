package com.sk89q.craftbook.circuits.ic;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ICDocsParser {

    private static CircuitCore core = CircuitCore.inst();

    public static void generateICDocs(Player player, String id) {

        RegisteredICFactory ric = core.getIcManager().registered.get(id.toLowerCase());
        if (ric == null) {
            try {
                ric = core.getIcManager().registered.get(core.getSearchID(player, id));
                if (ric == null) {
                    player.sendMessage(ChatColor.RED + "Invalid IC!");
                    return;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid IC!");
                return;
            }
        }
        try {
            IC ic = ric.getFactory().create(null);
            player.sendMessage(" "); // To space the area
            player.sendMessage(ChatColor.BLUE + ic.getTitle() + " (" + ric.getId() + ") Documentation");
            if (CraftBookPlugin.inst().getConfiguration().ICShortHandEnabled && ric.getShorthand() != null) {
                player.sendMessage(ChatColor.YELLOW + "Shorthand: =" + ric.getShorthand());
            }
            player.sendMessage(ChatColor.YELLOW + "Desc: " + ric.getFactory().getShortDescription());
            if (ric.getFactory().getLineHelp()[0] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 3: " + parseLine(ric.getFactory().getLineHelp()[0]));
            } else {
                player.sendMessage(ChatColor.GRAY + "Line 3: Blank.");
            }
            if (ric.getFactory().getLineHelp()[1] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 4: " + parseLine(ric.getFactory().getLineHelp()[1]));
            } else {
                player.sendMessage(ChatColor.GRAY + "Line 4: Blank.");
            }
            player.sendMessage(ChatColor.AQUA + "Wiki: " + "http://wiki.sk89q.com/wiki/CraftBook/" + ric.getId()
                    .toUpperCase());
        } catch (Exception ignored) {
        }
    }

    private static String parseLine(String line) {

        if(line.contains("+o"))
            line = ChatColor.GREEN + line + " (Optional)";

        line = line.replace("{", ChatColor.GREEN + "");
        line = line.replace("}", ChatColor.YELLOW + "");

        return line.replace("+o", "");
    }
}