package com.sk89q.craftbook.bukkit;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.config.YAMLConfiguration;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * A CraftBook implementation of {@link com.sk89q.worldedit.bukkit.BukkitConfiguration}.
 */
public class BukkitConfiguration extends YAMLConfiguration {

    public boolean enableCircuits;
    public boolean enableMechanisms;
    public boolean enableVehicles;

    public boolean noOpPermissions;
    public boolean indirectRedstone;
    public boolean useBlockDistance;
    public boolean safeDestruction;
    public int stThinkRate;
    public boolean obeyWorldguard;

    public boolean updateNotifier;

    public String language;
    public List<String> languages;

    private final CraftBookPlugin plugin;

    public BukkitConfiguration(YAMLProcessor config, CraftBookPlugin plugin) {

        super(config, plugin.getLogger());
        this.plugin = plugin;
    }

    @Override
    public void load() {

        try {
            config.load();
        } catch (IOException e) {
            logger.severe("Error loading CraftBook configuration: " + e);
            BukkitUtil.printStacktrace(e);
        }

        config.setHeader(
                "# CraftBook Configuration for Bukkit. Generated for version: " + CraftBookPlugin.inst().getDescription().getVersion(),
                "# This configuration will automatically add new configuration options for you,",
                "# So there is no need to regenerate this configuration unless you need to.",
                "# More information about these configuration nodes are available at...",
                "# http://wiki.sk89q.com/wiki/CraftBook/Configuration",
                "",
                "");

        enableCircuits = config.getBoolean("enable-circuits", true);
        enableMechanisms = config.getBoolean("enable-mechanisms", true);
        enableVehicles = config.getBoolean("enable-vehicles", true);

        config.setComment("st-think-ticks", "WARNING! Changing this can result in all ST mechanics acting very weirdly, only change this if you know what you are doing!");
        stThinkRate = config.getInt("st-think-ticks", 2);
        updateNotifier = config.getBoolean("notify-updates", true);
        safeDestruction = config.getBoolean("safe-destruction", true);
        noOpPermissions = config.getBoolean("no-op-permissions", false);
        indirectRedstone = config.getBoolean("indirect-redstone", false);
        useBlockDistance = config.getBoolean("use-block-distance", false);
        obeyWorldguard = config.getBoolean("check-worldguard-flags", true);
        language = config.getString("language", "en_US");
        languages = config.getStringList("languages", Arrays.asList("en_US"));

        super.load();
    }

    @Override
    public File getWorkingDirectory() {

        return plugin.getDataFolder();
    }
}