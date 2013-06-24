package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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

        if(player != null) {
            line = parsePlayerTags(line, player);
        }
        line = parseVariables(line, player);

        return line;
    }

    public static String parsePlayerTags(String line, Player player) {

        line = line.replace("@p", player.getName());
        line = line.replace("@p.l", player.getLocation().getX() + ":" + player.getLocation().getY() + ":" + player.getLocation().getZ());

        return line;
    }

    public static List<String> getPossibleVariables(String line) {

        List<String> variables = new ArrayList<String>();

        for(String bit : RegexUtil.PERCENT_PATTERN.split(line))
            variables.add(bit);

        if(!variables.isEmpty())
            variables.remove(0);
        if(!variables.isEmpty())
            variables.remove(variables.size() - 1);

        return variables;
    }

    public static String parseVariables(String line, Player player) {

        if(CraftBookPlugin.inst() == null)
            return line;

        for(String var : getPossibleVariables(line)) {

            String key, value;

            if(var.contains("|")) {
                String[] bits = RegexUtil.PIPE_PATTERN.split(var);
                key = bits[0];
                value = bits[1];
            } else {
                key = "global";
                value = var;
            }

            if(player != null)
                if(!player.hasPermission("craftbook.variables.use." + key))
                    continue;

            for(Entry<Tuple2<String, String>, String> bit : CraftBookPlugin.inst().getVariableStore().entrySet()) {
                if(bit.getKey().b.equalsIgnoreCase(key) && bit.getKey().a.equalsIgnoreCase(value))
                    line = line.replace("%" + var + "|" + key + "%", bit.getValue());
            }
        }

        return line;
    }
}