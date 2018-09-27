package com.sk89q.craftbook.util;

import com.sk89q.craftbook.util.compat.CraftBookCompatability;
import com.sk89q.craftbook.util.compat.NoCheatPlusCompatability;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides hooks into many other plugins that may interfere with CraftBook.
 */
public final class CompatabilityUtil {

    private static Set<CraftBookCompatability> compatChecks = new HashSet<>();

    /**
     * The initialization method for this Util.
     * 
     * This util needs initialization as it must check for available compatability handlers, and enable them if possible.
     */
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("NoCheatPlus") != null)
            compatChecks.add(new NoCheatPlusCompatability());
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