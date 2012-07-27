package com.sk89q.craftbook;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * @author Me4502
 */
public class BaseConfiguration {

    public BaseConfiguration(FileConfiguration cfg, File dataFolder) {

        commonSettings = new CommonSettings(cfg);
    }

    public final CommonSettings commonSettings;

    //General settings
    public class CommonSettings {

        public final String language;
        public final boolean obeyCancelled;
        public final boolean opPerms;

        private CommonSettings(FileConfiguration cfg) {

            language = getString(cfg, "language", "en_US");
            obeyCancelled = getBoolean(cfg, "obey-cancelled", true);
            opPerms = getBoolean(cfg, "op-perms", true);
        }
    }

    public int getInt(FileConfiguration cfg, String name, int def) {

        int it = cfg.getInt(name, def);
        cfg.set(name, it);
        return it;
    }

    public double getDouble(FileConfiguration cfg, String name, double def) {

        double it = cfg.getDouble(name, def);
        cfg.set(name, it);
        return it;
    }

    public boolean getBoolean(FileConfiguration cfg, String name, boolean def) {

        boolean it = cfg.getBoolean(name, def);
        cfg.set(name, it);
        return it;
    }

    public String getString(FileConfiguration cfg, String name, String def) {

        String it = cfg.getString(name, def);
        cfg.set(name, it);
        return it;
    }
}