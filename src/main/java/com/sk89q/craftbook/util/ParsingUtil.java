package com.sk89q.craftbook.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.variables.VariableCommands;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public final class ParsingUtil {

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

        line = StringUtils.replace(line, "@p.l", player.getLocation().getX() + ":" + player.getLocation().getY() + ":" + player.getLocation().getZ());
        line = StringUtils.replace(line, "@p.x", String.valueOf(player.getLocation().getX()));
        line = StringUtils.replace(line, "@p.y", String.valueOf(player.getLocation().getY()));
        line = StringUtils.replace(line, "@p.z", String.valueOf(player.getLocation().getZ()));
        line = StringUtils.replace(line, "@p.bx", String.valueOf(player.getLocation().getBlockX()));
        line = StringUtils.replace(line, "@p.by", String.valueOf(player.getLocation().getBlockY()));
        line = StringUtils.replace(line, "@p.bz", String.valueOf(player.getLocation().getBlockZ()));
        line = StringUtils.replace(line, "@p.w", String.valueOf(player.getLocation().getWorld().getName()));
        line = StringUtils.replace(line, "@p.u", player.getUniqueId().toString());
        line = StringUtils.replace(line, "@p.i", CraftBookPlugin.inst().getUUIDMappings().getCBID(player.getUniqueId()));
        line = StringUtils.replace(line, "@p", player.getName());

        return line;
    }

    public static List<String> getPossibleVariables(String line) {

        if(!line.contains("%"))
            return new ArrayList<>();

        return variableFinderCache.getUnchecked(line);
    }

    private static final LoadingCache<String, List<String>> variableFinderCache = CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<String, List<String>>() {
        @Override
        public List<String> load (String line) throws Exception {

            List<String> variables = new ArrayList<>();

            for(String bit : RegexUtil.PERCENT_PATTERN.split(line)) {
                if(line.indexOf(bit) > 0 && line.charAt(line.indexOf(bit)-1) == '\\') continue;
                if(!bit.trim().isEmpty() && !bit.trim().equals("|"))
                    variables.add(bit.trim());
            }

            return variables;
        }
    });

    public static String parseVariables(String line, CommandSender player) {

        if(CraftBookPlugin.inst() == null || VariableManager.instance == null || VariableManager.instance.getVariableStore().isEmpty())
            return line;

        for(String var : getPossibleVariables(line)) {

            CraftBookPlugin.logDebugMessage("Possible variable: " + var + " detected!", "variables.line-parsing");

            String key, value;

            if(var.contains("|")) {
                String[] bits = RegexUtil.PIPE_PATTERN.split(var);
                if(bits.length < 2) {
                    key = "global";
                    value = var;
                } else {
                    key = bits[0];
                    value = bits[1];
                }
                CraftBookPlugin.logDebugMessage("Variable " + value + " at " + key + " detected!", "variables.line-parsing");
            } else {
                key = "global";
                value = var;
                CraftBookPlugin.logDebugMessage("Global Variable " + value + " detected!", "variables.line-parsing");
            }

            if(player != null)
                if(!VariableCommands.hasVariablePermission(player, key, value, "use"))
                    continue;
            CraftBookPlugin.logDebugMessage(var + " permissions granted!", "variables.line-parsing");

            for(Entry<Tuple2<String, String>, String> bit : VariableManager.instance.getVariableStore().entrySet()) {
                if(bit.getKey().b.equals(key) && bit.getKey().a.equals(value)) {
                    line = StringUtils.replace(line, "%" + var + "%", bit.getValue());
                }
            }
        }

        return StringUtils.replace(line, "\\%", "%");
    }
}