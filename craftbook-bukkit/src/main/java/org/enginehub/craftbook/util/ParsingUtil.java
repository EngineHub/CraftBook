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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.enginehub.craftbook.bukkit.mechanics.variables.BukkitVariableManager;
import org.jspecify.annotations.Nullable;

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
    @Deprecated
    public static String parseLine(String line, @Nullable Player player) {
        checkNotNull(line);
        return PlainTextComponentSerializer.plainText().serialize(parseLine(PlainTextComponentSerializer.plainText().deserialize(line), player));
    }

    /**
     * Parses a line with all tags possible with given arguments.
     *
     * @param line The base line to start with.
     * @param player The player associated with the line
     * @return The same line, with parsing completed
     */
    public static Component parseLine(Component line, @Nullable Player player) {
        checkNotNull(line);
        if (player != null) {
            line = parsePlayerTags(line, player);
        }
        if (BukkitVariableManager.instance != null) {
            line = BukkitVariableManager.instance.renderVariables(line, player);
        }

        return line;
    }

    private static Component parsePlayerTags(Component line, Player player) {
        Location location = player.getLocation();

        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.l")
            .replacement(location.getX() + "," + location.getY() + "," + location.getZ()).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.c")
            .replacement(location.getX() + " " + location.getY() + " " + location.getZ()).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.x")
            .replacement(String.valueOf(location.getX())).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.y")
            .replacement(String.valueOf(location.getY())).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.z")
            .replacement(String.valueOf(location.getZ())).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.bx")
            .replacement(String.valueOf(location.getBlockX())).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.by")
            .replacement(String.valueOf(location.getBlockY())).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.bz")
            .replacement(String.valueOf(location.getBlockZ())).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.w")
            .replacement(((World) location.getExtent()).getName()).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p.u")
            .replacement(player.getUniqueId().toString()).build());
        line = line.replaceText(TextReplacementConfig.builder().matchLiteral("@p")
            .replacement(player.getName()).build());

        return line;
    }
}
