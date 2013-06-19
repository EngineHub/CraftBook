package com.sk89q.craftbook.util;

import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ParsingUtil {

    /**
     * Parses a line with all tags possible with given arguments.
     * 
     * @param line The base line to start with.
     * @param player The player associated with the line (Can be null)
     * 
     * @return
     */
    public static String parseLine(String line, Player player) {

        if(player != null)
            line = parsePlayerTags(line, player);
        line = parseGlobalVariables(line);

        return line;
    }

    public static String parsePlayerTags(String line, Player player) {

        line = line.replace("@p", player.getName());
        line = line.replace("@p.l", player.getLocation().getX() + ":" + player.getLocation().getY() + ":" + player.getLocation().getZ());

        return line;
    }

    public static String parseGlobalVariables(String line) {

        for(Tuple2<String, String> key : CraftBookPlugin.inst().variableStore.keySet())
            if(key.a.equalsIgnoreCase("global"))
                line = line.replace("%" + key.b  + "%", CraftBookPlugin.inst().variableStore.get(key));

        return line;
    }
}