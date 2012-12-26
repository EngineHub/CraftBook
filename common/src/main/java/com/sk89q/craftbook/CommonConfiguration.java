package com.sk89q.craftbook;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Silthus
 */
public class CommonConfiguration extends BaseConfiguration {

    public CommonConfiguration(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
    }

    public List<String> languages;
    public String language;
    public boolean opPerms;
    public boolean useBlockDistance;
    public boolean checkWGRegions;
    public boolean experimentalRepeaters;
    public boolean indirectRedstone;

    @Override
    public void load() {

        languages = getStringList("languages", new ArrayList<String>(Arrays.asList("en_US")));
        language = getString("language", "en_US");
        opPerms = getBoolean("op-perms", true);
        useBlockDistance = getBoolean("use-block-radius", false);
        checkWGRegions = getBoolean("check-worldguard-flags", true);
        experimentalRepeaters = getBoolean("experimental-repeater-support", false);
        indirectRedstone = getBoolean("indirect-redstone-support", false);
    }
}
