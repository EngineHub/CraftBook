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
            language      = cfg.getString("language",          "en_US");
            cfg.set("language", language);
        }
    }
}