package com.sk89q.craftbook;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Silthus
 */
public class CommonConfiguration extends BaseConfiguration {

    public CommonConfiguration(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);

        languages = getStringList("languages", new ArrayList<String>(Arrays.asList("en_US")));
        language = getString("language", "en_US");
        opPerms = getBoolean("op-perms", true);
        useBlockDistance = getBoolean("use-block-radius", false);
        checkWGRegions = getBoolean("check-worldguard-flags", true);
        experimentalRepeaters = getBoolean("experimental-repeater-support", false);
        indirectRedstone = getBoolean("indirect-redstone-support", false);
    }

    public final List<String> languages;
    public final String language;
    public final boolean opPerms;
    public final boolean useBlockDistance;
    public final boolean checkWGRegions;
    public final boolean experimentalRepeaters;
    public final boolean indirectRedstone;
}
