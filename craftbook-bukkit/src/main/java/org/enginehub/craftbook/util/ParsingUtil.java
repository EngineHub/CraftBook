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

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.variables.VariableManager;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ParsingUtil {

    private ParsingUtil() {
    }

    /**
     * Parses a line with all tags possible with given arguments.
     *
     * @param line The base line to start with.
     * @param player The player associated with the line
     * @return The same line, with parsing completed
     */
    public static String parseLine(String line, @Nullable Player player) {
        checkNotNull(line);
        if (player != null) {
            line = parsePlayerTags(line, player);
        }
        if (VariableManager.instance != null) {
            line = VariableManager.renderVariables(line, player == null ? null : CraftBookPlugin.inst().wrapPlayer(player));
        }

        return line;
    }

    private static String parsePlayerTags(String line, Player player) {
        line = StringUtils.replace(line, "@p.l", player.getLocation().getX() + ":" + player.getLocation().getY() + ":" + player.getLocation().getZ());
        line = StringUtils.replace(line, "@p.x", String.valueOf(player.getLocation().getX()));
        line = StringUtils.replace(line, "@p.y", String.valueOf(player.getLocation().getY()));
        line = StringUtils.replace(line, "@p.z", String.valueOf(player.getLocation().getZ()));
        line = StringUtils.replace(line, "@p.bx", String.valueOf(player.getLocation().getBlockX()));
        line = StringUtils.replace(line, "@p.by", String.valueOf(player.getLocation().getBlockY()));
        line = StringUtils.replace(line, "@p.bz", String.valueOf(player.getLocation().getBlockZ()));
        line = StringUtils.replace(line, "@p.w", player.getLocation().getWorld().getName());
        line = StringUtils.replace(line, "@p.u", player.getUniqueId().toString());
        line = StringUtils.replace(line, "@p.i", CraftBookPlugin.inst().getUUIDMappings().getCBID(player.getUniqueId()));
        line = StringUtils.replace(line, "@p", player.getName());

        return line;
    }

}