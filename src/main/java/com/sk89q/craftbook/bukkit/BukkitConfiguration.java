package com.sk89q.craftbook.bukkit;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sk89q.craftbook.util.config.YAMLConfiguration;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * A CraftBook implementation of {@link com.sk89q.worldedit.bukkit.BukkitConfiguration}.
 */
public class BukkitConfiguration extends YAMLConfiguration {

    public boolean enableCircuits = true;
    public boolean enableMechanisms = true;
    public boolean enableVehicles = true;

    public boolean noOpPermissions = false;
    public boolean indirectRedstone = false;
    public boolean useBlockDistance = false;
    public boolean safeDestruction = true;

    public boolean obeyWorldguard = true;

    public String language = "en_US";
    public List<String> languages = Arrays.asList("en_US");

    private final CraftBookPlugin plugin;

    public BukkitConfiguration(YAMLProcessor config, CraftBookPlugin plugin) {

        super(config, plugin.getLogger());
        this.plugin = plugin;
    }

    @Override
    public void load() {

        enableCircuits = config.getBoolean("enable-circuits", true);
        enableMechanisms = config.getBoolean("enable-mechanics", true);
        enableVehicles = config.getBoolean("enable-vehicles", true);

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