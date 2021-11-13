/*
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

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.apache.commons.lang.StringUtils;
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
            line = VariableManager.renderVariables(line, player);
        }

        return line;
    }

    private static String parsePlayerTags(String line, Player player) {
        Location location = player.getLocation();

        line = line.replace("@p.l", location.getX() + "," + location.getY() + "," + location.getZ());
        line = line.replace("@p.c", location.getX() + " " + location.getY() + " " + location.getZ());
        line = line.replace("@p.x", String.valueOf(location.getX()));
        line = line.replace("@p.y", String.valueOf(location.getY()));
        line = line.replace("@p.z", String.valueOf(location.getZ()));
        line = line.replace("@p.bx", String.valueOf(location.getBlockX()));
        line = line.replace("@p.by", String.valueOf(location.getBlockY()));
        line = line.replace("@p.bz", String.valueOf(location.getBlockZ()));
        line = line.replace("@p.w", ((World) location.getExtent()).getName());
        line = line.replace("@p.u", player.getUniqueId().toString());
        line = line.replace("@p", player.getName());

        return line;
    }
}
