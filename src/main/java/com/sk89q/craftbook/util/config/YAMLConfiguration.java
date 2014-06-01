package com.sk89q.craftbook.util.config;

import java.util.logging.Logger;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * A implementation of YAML based off of {@link com.sk89q.worldedit.util.YAMLConfiguration} for CraftBook.
 */
public class YAMLConfiguration extends LocalConfiguration {

    public final YAMLProcessor config;
    protected final Logger logger;

    public YAMLConfiguration(YAMLProcessor config, Logger logger) {

        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load() {

        config.save(); //Save all the added values.
    }
}