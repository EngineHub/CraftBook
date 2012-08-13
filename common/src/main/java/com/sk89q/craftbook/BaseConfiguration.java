package com.sk89q.craftbook;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Me4502
 */
public class BaseConfiguration {

    public final FileConfiguration cfg;

    public BaseConfiguration(FileConfiguration cfg, File dataFolder) {

        this.cfg = cfg;
        commonSettings = new CommonSettings();
    }

    public final CommonSettings commonSettings;

    //General settings
    public class CommonSettings {

        public final String language;
        public final boolean opPerms;
        public final boolean useBlockDistance;

        private CommonSettings() {

            language = getString("language", "en_US");
            opPerms = getBoolean("op-perms", true);
            useBlockDistance = getBoolean("use-block-radius", false);
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

    public Set<Integer> getIntegerSet(String name, List<Integer> def) {

        List<Integer> tids = cfg.getIntegerList(name);
        if (tids == null || tids.isEmpty() || tids.size() < 1) tids = def;
        Set<Integer> allowedBlocks = new HashSet<Integer>();
        for (Integer tid : tids) allowedBlocks.add(tid);
        cfg.set(name, tids);
        return allowedBlocks;

    }

    public Set<Material> getMaterialSet(String name, List<Integer> def) {

        List<Integer> tids = cfg.getIntegerList(name);
        if (tids == null || tids.isEmpty() || tids.size() < 1) tids = def;
        Set<Material> allowedBlocks = new HashSet<Material>();
        for (Integer tid : tids) allowedBlocks.add(Material.getMaterial(tid));
        cfg.set(name, tids);
        return Collections.unmodifiableSet(allowedBlocks);
    }
}