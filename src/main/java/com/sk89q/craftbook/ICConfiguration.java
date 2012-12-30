package com.sk89q.craftbook;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.circuits.ic.RegisteredICFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ICConfiguration extends BaseConfiguration {

    /**
     * Initialized IC Config. Run after IC's are registered.
     *
     * @param cfg
     * @param dataFolder
     */
    public ICConfiguration(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
    }

    @Override
    public void load() {

        for (RegisteredICFactory factory : ((CircuitCore) CircuitCore.inst()).getICList()) {
            if (factory.getId().startsWith("MCA")) {
                continue;
            }
            if (factory.getFactory().needsConfiguration())
                factory.getFactory().addConfiguration(new BaseConfiguration.BaseConfigurationSection(factory.getId()));
        }
    }
}