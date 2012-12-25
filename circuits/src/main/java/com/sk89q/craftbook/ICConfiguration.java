package com.sk89q.craftbook;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.RegisteredICFactory;

public class ICConfiguration extends BaseConfiguration {

    /**
     * Initialized IC Config. Run after IC's are registered.
     * 
     * @param cfg
     * @param dataFolder
     */
    public ICConfiguration (FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
    }

    @Override
    public void load () {

        for (RegisteredICFactory factory : CircuitsPlugin.getInst().icManager.registered.values()) {
            if (factory.getId().startsWith("MCA")) {
                continue;
            }
            if (factory.getFactory().needsConfiguration())
                factory.getFactory().addConfiguration(new BaseConfiguration.BaseConfigurationSection(factory.getId()));
        }
    }
}