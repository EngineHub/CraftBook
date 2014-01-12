package com.sk89q.craftbook.util.config;

import java.io.IOException;
import java.util.logging.Logger;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.circuits.ic.ConfigurableIC;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.circuits.ic.RegisteredICFactory;
import com.sk89q.util.yaml.YAMLProcessor;

public class YAMLICConfiguration extends LocalConfiguration {

    protected final YAMLProcessor config;
    protected final Logger logger;

    public YAMLICConfiguration(YAMLProcessor config, Logger logger) {

        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load () {

        try {
            config.load();
        } catch (IOException e) {
            logger.severe("Error loading CraftBook IC configuration: " + e);
            e.printStackTrace();
        }

        for (RegisteredICFactory factory : ICManager.INSTANCE.getICList())
            if (factory.getFactory() instanceof ConfigurableIC)
                ((ConfigurableIC) factory.getFactory()).addConfiguration(config, "ics." + factory.getId() + ".");

        config.save(); //Save all the added values.
    }
}