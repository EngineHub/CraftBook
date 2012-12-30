package com.sk89q.craftbook.util.config;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.circuits.ic.RegisteredICFactory;
import com.sk89q.util.yaml.YAMLProcessor;

public class YAMLICConfiguration extends LocalConfiguration {

    protected final YAMLProcessor config;
    protected final Logger logger;
    private FileHandler logFileHandler;

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

        for (RegisteredICFactory factory : CircuitCore.inst().getICList()) {
            if (factory.getId().startsWith("MCA")) {
                continue;
            }
            if (factory.getFactory().needsConfiguration())
                factory.getFactory().addConfiguration(config, "ics." + factory.getId() + ".");
        }

        config.save(); //Save all the added values.
    }

    public void unload() {

        if (logFileHandler != null) {
            logFileHandler.close();
        }
    }
}