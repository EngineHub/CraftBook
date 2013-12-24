package com.sk89q.craftbook.util.config;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.common.VariableManager;
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

        if(config.getKeys("variables") == null) return;
        for(String key : config.getKeys("variables")) {

            String[] keys = RegexUtil.PIPE_PATTERN.split(key, 2);
            if(keys.length == 1)
                keys = new String[]{"global",key};

            if(RegexUtil.VARIABLE_KEY_PATTERN.matcher(keys[0]).find() && RegexUtil.VARIABLE_KEY_PATTERN.matcher(keys[1]).find() && RegexUtil.VARIABLE_VALUE_PATTERN.matcher(String.valueOf(config.getProperty("variables." + key))).find()) {
                VariableManager.instance.setVariable(keys[1], keys[0], String.valueOf(config.getProperty("variables." + key)));
            }
        }
    }

    public void save() {

        for(Entry<Tuple2<String, String>, String> var : VariableManager.instance.getVariableStore().entrySet()) {

            if(RegexUtil.VARIABLE_KEY_PATTERN.matcher(var.getKey().a).find() && RegexUtil.VARIABLE_KEY_PATTERN.matcher(var.getKey().b).find() && RegexUtil.VARIABLE_VALUE_PATTERN.matcher(var.getValue()).find())
                config.setProperty("variables." + var.getKey().b + "|" + var.getKey().a, var.getValue());
        }
        config.save();
    }
}