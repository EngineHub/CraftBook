package com.sk89q.craftbook;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public class BaseConfiguration {

    public BaseConfiguration(FileConfiguration cfg, File dataFolder) {
        commonSettings = new CommonSettings(cfg);
    }

    public final CommonSettings commonSettings;

    //General settings
    public class CommonSettings {
        public final String language;

        private CommonSettings(FileConfiguration cfg) {
            language = getString(cfg,"language","en_US");
        }
    }

    public int getInt(FileConfiguration cfg, String name, int def) {
        int it = cfg.getInt(name);
        cfg.set(name, it);
        return it;
    }

    public boolean getBoolean(FileConfiguration cfg, String name, boolean def) {
        boolean it = cfg.getBoolean(name);
        cfg.set(name, it);
        return it;
    }

    public String getString(FileConfiguration cfg, String name, String def) {
        String it = cfg.getString(name,def);
        cfg.set(name, it);
        return it;
    }
}