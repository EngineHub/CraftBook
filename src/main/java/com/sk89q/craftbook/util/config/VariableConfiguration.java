package com.sk89q.craftbook.util.config;

import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
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
            for(String key : config.getKeys("variables")) {

                if(RegexUtil.VARIABLE_PATTERN.matcher(key).find() && RegexUtil.VARIABLE_PATTERN.matcher(String.valueOf(config.getProperty("variables." + key))).find())
                    CraftBookPlugin.inst().variableStore.put(key, String.valueOf(config.getProperty("variables." + key)));
            }
        } catch(Exception e) {
            logger.severe("An error occured loading variables!");
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