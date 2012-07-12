package com.sk89q.craftbook;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public class CommonConfiguration {

    public CommonConfiguration(FileConfiguration cfg, File dataFolder) {
        this.dataFolder = dataFolder;

        commonSettings = new CommonSettings(cfg);
    }

    public final File dataFolder;
    public final CommonSettings commonSettings;

    //General settings
    public class CommonSettings {
        public final String language;

        private CommonSettings(FileConfiguration cfg) {
            language      = cfg.getString("language",          "en_US");
        }
    }
}