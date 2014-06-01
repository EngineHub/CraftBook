package com.sk89q.craftbook.mechanics.ic;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ICDocsParser {

    public static void generateICDocs(Player player, String id) {

        RegisteredICFactory ric = ICManager.inst().registered.get(id.toLowerCase(Locale.ENGLISH));
        if (ric == null) {
            try {
                ric = ICManager.inst().registered.get(ICManager.inst().getSearchID(player, id));
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
            if (ICMechanic.instance.shortHand && ric.getShorthand() != null) {
                player.sendMessage(ChatColor.YELLOW + "Shorthand: =" + ric.getShorthand());
            }
            player.sendMessage(ChatColor.YELLOW + "Desc: " + ric.getFactory().getShortDescription());
            if (ric.getFactory().getLineHelp()[0] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 3: " + parseLine(ric.getFactory().getLineHelp()[0]));
            } else {
                player.sendMessage(ChatColor.DARK_GRAY + "Line 3: Blank.");
            }
            if (ric.getFactory().getLineHelp()[1] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 4: " + parseLine(ric.getFactory().getLineHelp()[1]));
            } else {
                player.sendMessage(ChatColor.DARK_GRAY + "Line 4: Blank.");
            }
            player.sendMessage(ChatColor.AQUA + "Wiki: " + CraftBookPlugin.getWikiDomain() + "/" + ric.getId().toUpperCase(Locale.ENGLISH));
        } catch (Exception ignored) {
        }
    }

    private static String parseLine(String line) {

        if(line.contains("+o"))
            line = ChatColor.GRAY + line + " (Optional)";

        line = StringUtils.replace(line, "{", ChatColor.GRAY + "");
        line = StringUtils.replace(line, "}", ChatColor.YELLOW + "");

        return StringUtils.replace(line, "+o", "");
    }
}