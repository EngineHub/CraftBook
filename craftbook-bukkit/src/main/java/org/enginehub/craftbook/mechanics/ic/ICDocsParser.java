/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.ic;

import java.util.Locale;

import com.sk89q.worldedit.extension.platform.Actor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ICDocsParser {

    private ICDocsParser() {
    }

    public static void generateICDocs(Actor player, String id) {

        RegisteredICFactory ric = ICManager.inst().registered.get(id.toLowerCase(Locale.ENGLISH));
        if (ric == null) {
            try {
                ric = ICManager.inst().registered.get(ICManager.inst().getSearchID(id));
                if (ric == null) {
                    player.printError("Invalid IC!");
                    return;
                }
            } catch (Exception e) {
                player.printError("Invalid IC!");
                return;
            }
        }
        try {
            IC ic = ric.getFactory().create(null);
            player.print(" "); // To space the area
            player.print(ChatColor.BLUE + ic.getTitle() + " (" + ric.getId() + ") Documentation");
            if (ICMechanic.instance.shortHand && ric.getShorthand() != null) {
                player.print("Shorthand: =" + ric.getShorthand());
            }
            player.print("Desc: " + ric.getFactory().getShortDescription());
            if (ric.getFactory().getLineHelp()[0] != null) {
                player.print("Line 3: " + parseLine(ric.getFactory().getLineHelp()[0]));
            } else {
                player.printDebug("Line 3: Blank.");
            }
            if (ric.getFactory().getLineHelp()[1] != null) {
                player.print("Line 4: " + parseLine(ric.getFactory().getLineHelp()[1]));
            } else {
                player.printDebug("Line 4: Blank.");
            }
            player.print(ChatColor.AQUA + "Wiki: " + CraftBookPlugin.getDocsDomain() + "/" + ric.getId().toUpperCase(Locale.ENGLISH));
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