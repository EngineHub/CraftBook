package com.sk89q.craftbook.util.config;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.util.yaml.YAMLProcessor;

public class VariableConfiguration {

    public final YAMLProcessor config;
    protected final Logger logger;

    public VariableConfiguration(YAMLProcessor config, Logger logger) {

        this.config = config;
        this.logger = logger;
    }

    public void load() {

        try {
            config.load();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
            return;
        }

        for(String key : config.getKeys("variables")) {

            String[] keys = RegexUtil.COLON_PATTERN.split(key, 2);
            if(keys.length == 1)
                keys = new String[]{"global",key};

            if(RegexUtil.VARIABLE_KEY_PATTERN.matcher(keys[0]).find() && RegexUtil.VARIABLE_KEY_PATTERN.matcher(keys[1]).find() && RegexUtil.VARIABLE_VALUE_PATTERN.matcher(String.valueOf(config.getProperty("variables." + key))).find()) {
                CraftBookPlugin.inst().variableStore.put(new Tuple2<String, String>(keys[0],keys[1]), String.valueOf(config.getProperty("variables." + key)));
            }
        }
    }

    public void save() {

        for(Entry<Tuple2<String, String>, String> var : CraftBookPlugin.inst().variableStore.entrySet()) {

            if(RegexUtil.VARIABLE_KEY_PATTERN.matcher(var.getKey().a).find() && RegexUtil.VARIABLE_KEY_PATTERN.matcher(var.getKey().b).find() && RegexUtil.VARIABLE_VALUE_PATTERN.matcher(var.getValue()).find())
                config.setProperty("variables." + var.getKey().a + ":" + var.getKey().b, var.getValue());
        }
        config.save();
    }
}