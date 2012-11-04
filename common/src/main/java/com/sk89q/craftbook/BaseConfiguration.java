package com.sk89q.craftbook;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Me4502
 */
public abstract class BaseConfiguration {

    public final FileConfiguration cfg;
    public final File dataFolder;

    public BaseConfiguration(FileConfiguration cfg, File dataFolder) {

        this.cfg = cfg;
        this.dataFolder = dataFolder;

        load();
    }

    public abstract void load();

    public boolean reload() {

        try {
            load();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getInt(String name, int def) {

        int it = cfg.getInt(name, def);
        cfg.set(name, it);
        return it;
    }

    public double getDouble(String name, double def) {

        double it = cfg.getDouble(name, def);
        cfg.set(name, it);
        return it;
    }

    public boolean getBoolean(String name, boolean def) {

        boolean it = cfg.getBoolean(name, def);
        cfg.set(name, it);
        return it;
    }

    public String getString(String name, String def) {

        String it = cfg.getString(name, def);
        cfg.set(name, it);
        return it;
    }

    public List<String> getStringList(String name, List<String> def) {

        List<String> it = cfg.getStringList(name);
        if (it == null || it.size() == 0) {
            it = def;
        }
        cfg.set(name, it);
        return it;
    }

    public Set<Integer> getIntegerSet(String name, List<Integer> def) {

        List<Integer> tids = cfg.getIntegerList(name);
        if (tids == null || tids.isEmpty() || tids.size() < 1) {
            tids = def;
        }
        Set<Integer> allowedBlocks = new HashSet<Integer>();
        for (Integer tid : tids) {
            allowedBlocks.add(tid);
        }
        cfg.set(name, tids);
        return allowedBlocks;
    }

    public class BaseConfigurationSection {

        public final ConfigurationSection section;

        public BaseConfigurationSection(String section) {

            if (!cfg.isConfigurationSection(section)) {
                cfg.createSection(section);
            }
            this.section = cfg.getConfigurationSection(section);
        }

        public int getInt(String name, int def) {

            int it = section.getInt(name, def);
            section.set(name, it);
            return it;
        }

        public double getDouble(String name, double def) {

            double it = section.getDouble(name, def);
            section.set(name, it);
            return it;
        }

        public boolean getBoolean(String name, boolean def) {

            boolean it = section.getBoolean(name, def);
            section.set(name, it);
            return it;
        }

        public String getString(String name, String def) {

            String it = section.getString(name, def);
            section.set(name, it);
            return it;
        }

        public List<String> getStringList(String name, List<String> def) {

            List<String> it = section.getStringList(name);
            if (it == null || it.size() == 0) {
                it = def;
            }
            section.set(name, it);
            return it;
        }

        public Set<Integer> getIntegerSet(String name, List<Integer> def) {

            List<Integer> tids = section.getIntegerList(name);
            if (tids == null || tids.isEmpty() || tids.size() < 1) {
                tids = def;
            }
            Set<Integer> allowedBlocks = new HashSet<Integer>();
            for (Integer tid : tids) {
                allowedBlocks.add(tid);
            }
            section.set(name, tids);
            return allowedBlocks;
        }
    }
}