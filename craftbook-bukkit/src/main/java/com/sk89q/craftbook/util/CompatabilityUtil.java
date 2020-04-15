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

package com.sk89q.craftbook.util;

import com.sk89q.craftbook.util.compat.CraftBookCompatability;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides hooks into many other plugins that may interfere with CraftBook.
 */
public final class CompatabilityUtil {

    private CompatabilityUtil() {
    }

    private static Set<CraftBookCompatability> compatChecks = new HashSet<>();

    /**
     * The initialization method for this Util.
     * 
     * This util needs initialization as it must check for available compatability handlers, and enable them if possible.
     */
    public static void init() {
    }

    public static void disableInterferences(Player player) {

        for(CraftBookCompatability compat : compatChecks)
            compat.enable(player);
    }

    public static void enableInterferences(Player player) {
        for(CraftBookCompatability compat : compatChecks)
            compat.disable(player);
    }
}