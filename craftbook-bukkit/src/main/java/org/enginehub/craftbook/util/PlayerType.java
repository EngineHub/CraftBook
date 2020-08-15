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

package org.enginehub.craftbook.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;

import java.util.Locale;

public enum PlayerType {

    NAME('p'), UUID('u'), CBID('i'), GROUP('g'), PERMISSION_NODE('n'), TEAM('t'), ALL('a');

    PlayerType(char prefix) {

        this.prefix = prefix;
    }

    char prefix;

    public static PlayerType getFromChar(char c) {

        c = Character.toLowerCase(c);
        for (PlayerType t : values()) {
            if (t.prefix == c) {
                return t;
            }
        }
        return PlayerType.NAME;
    }

    public boolean doesPlayerPass(Player player, String line) {

        switch (this) {
            case GROUP:
                return CraftBookPlugin.inst().inGroup(player, line);
            case CBID:
                return CraftBookPlugin.inst().getUUIDMappings().getCBID(player.getUniqueId()).equals(line);
            case NAME:
                return player.getName().toLowerCase(Locale.ENGLISH).startsWith(line.toLowerCase(Locale.ENGLISH));
            case UUID:
                return player.getUniqueId().toString().toUpperCase(Locale.ENGLISH).startsWith(line.toUpperCase(Locale.ENGLISH));
            case PERMISSION_NODE:
                return player.hasPermission(line);
            case TEAM:
                try {
                    return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(line).hasEntry(player.getName());
                } catch (Exception e) {
                }
                break;
            case ALL:
                return true;
            default:
                return false;
        }

        return false;
    }
}