package com.sk89q.craftbook.util.config;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
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

            if(RegexUtil.VARIABLE_PATTERN.matcher(key).find() && RegexUtil.VARIABLE_PATTERN.matcher(String.valueOf(config.getProperty("variables." + key))).find())
                CraftBookPlugin.inst().variableStore.put(key, String.valueOf(config.getProperty("variables." + key)));
        }
    }

    public void save() {

        for(Entry<String, String> var : CraftBookPlugin.inst().variableStore.entrySet()) {

            if(RegexUtil.VARIABLE_PATTERN.matcher(var.getKey()).find() && RegexUtil.VARIABLE_PATTERN.matcher(var.getValue()).find())
                config.setProperty("variables." + var.getKey(), var.getValue());
        }
        config.save();
    }
}