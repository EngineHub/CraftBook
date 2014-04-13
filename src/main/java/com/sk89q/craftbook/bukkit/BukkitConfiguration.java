package com.sk89q.craftbook.bukkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

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
    public boolean advancedBlockChecks;
    public boolean pedanticBlockChecks;
    public boolean showPermissionMessages;
    public long signClickTimeout;
    public boolean convertNamesToCBID;

    public boolean updateNotifier;
    public boolean easterEggs;
    public boolean realisticRandoms;

    public String language;
    public List<String> languages;
    public boolean languageScanText;

    public boolean debugMode;
    public List<String> debugFlags;

    public String persistentStorageType;

    public BukkitConfiguration(YAMLProcessor config, Logger logger) {

        super(config, logger);
    }

    @Override
    public void load() {

        try {
            config.load();
        } catch (IOException e) {
            logger.severe("Error loading CraftBook configuration: " + e);
            BukkitUtil.printStacktrace(e);
        }

        config.setWriteDefaults(true);

        config.setHeader(
                "# CraftBook Configuration for Bukkit. Generated for version: " + (CraftBookPlugin.inst() == null ? CraftBookPlugin.getVersion() : CraftBookPlugin.inst().getDescription().getVersion()),
                "# This configuration will automatically add new configuration options for you,",
                "# So there is no need to regenerate this configuration unless you need to.",
                "# More information about these features are available at...",
                "# " + CraftBookPlugin.getWikiDomain() + "/Usage",
                "",
                "");

        config.setComment("enable-circuits", "If this is set to false, all circuit mechanics will be disabled, and circuit configuration will not do anything.");
        enableCircuits = config.getBoolean("enable-circuits", true);

        config.setComment("enable-mechanisms", "If this is set to false, all mechanics will be disabled, and mechanism configuration will not do anything.");
        enableMechanisms = config.getBoolean("enable-mechanisms", true);

        config.setComment("enable-vehicles", "If this is set to false, all vehicles mechanics will be disabled, and vehicle configuration will not do anything.");
        enableVehicles = config.getBoolean("enable-vehicles", true);

        config.setComment("st-think-ticks", "WARNING! Changing this can result in all ST mechanics acting very weirdly, only change this if you know what you are doing!");
        stThinkRate = config.getInt("st-think-ticks", 2);

        config.setComment("notify-updates", "Enables the update notifier. This checks for updates on start, and notifies anyone with the permission when they join. They can then use /cb update to download the update.");
        updateNotifier = config.getBoolean("notify-updates", true);

        config.setComment("safe-destruction", "Causes many mechanics to require sufficient blocks to function, for example gates, bridges and doors.");
        safeDestruction = config.getBoolean("safe-destruction", true);

        config.setComment("no-op-permissions", "If on, OP's will not default to have access to everything.");
        noOpPermissions = config.getBoolean("no-op-permissions", false);

        config.setComment("indirect-redstone", "Allows redstone not directly facing a mechanism to trigger said mechanism.");
        indirectRedstone = config.getBoolean("indirect-redstone", false);

        config.setComment("use-block-distance", "Rounds all distance equations to the block grid.");
        useBlockDistance = config.getBoolean("use-block-distance", false);

        config.setComment("check-worldguard-flags", "Checks to see if WorldGuard allows building/using in the area when activating mechanics.");
        obeyWorldguard = config.getBoolean("check-worldguard-flags", true);

        config.setComment("advanced-block-checks", "Use advanced methods to detect if a player can build or not. Use this if you use region protections other than WorldGuard, or experience issues with WorldGuard protection. This can add extra entries to Block Logging plugins when a mechanic is broken/placed.");
        advancedBlockChecks = config.getBoolean("advanced-block-checks", true);

        config.setComment("pedantic-block-checks", "In conjunction with advanced-block-checks, this option adds a few extra checks if you are experiencing compatibility issues with certain plugins that stop breaks/places/interacts.");
        pedanticBlockChecks = config.getBoolean("pedantic-block-checks", false);

        config.setComment("sign-click-timeout", "Make sure a player can only press signs so often.");
        signClickTimeout = config.getInt("sign-click-timeout", 10);

        config.setComment("language", "The default language for CraftBook. Note: This language needs to be in the 'languages' field for this to work.");
        language = config.getString("language", "en_US");

        config.setComment("languages", "A list of languages supported by craftbook, if a user requests a language not listed... They will see default.");
        languages = config.getStringList("languages", Arrays.asList("en_US"));

        config.setComment("scan-text-for-localization", "If enabled, CraftBook will scan messages sent to players for localizable text, instead of just checking if the entire message is localizable.");
        languageScanText = config.getBoolean("scan-text-for-localization", false);

        config.setComment("debug-mode", "Enable a mode that will print extra debug information to the console.");
        debugMode = config.getBoolean("debug-mode", false);

        config.setComment("debug-flags", "Enable certain debug types when debug mode is enabled.");
        debugFlags = config.getStringList("debug-flags", new ArrayList<String>());

        config.setComment("easter-eggs", "Enables random easter eggs. Can be from console messages on startup for a special occasion, to funny little effects with IC's and other mechanics (Always harmless, won't mess anything up)");
        easterEggs = config.getBoolean("easter-eggs", true);

        config.setComment("realistic-randoms", "Random numbers are much more random, with a small cost to CPU usage.");
        realisticRandoms = config.getBoolean("realistic-randoms", false);

        config.setComment("show-permission-messages", "Show messages when a player does not have permission to do something.");
        showPermissionMessages = config.getBoolean("show-permission-messages", true);

        config.setComment("persistent-storage-type", "PersistentStorage stores data that can be accessed across server restart. Method of PersistentStorage storage (Note: DUMMY is practically off, and may cause issues). Can currently be any of the following: YAML, DUMMY, SQLite");
        persistentStorageType = config.getString("persistent-storage-type", "YAML");

        config.setComment("convert-names-to-cbids", "Causes mechanics to attempt to convert names to use CBIDs. This can and should be disabled after you believe your servers transition to UUIDs v Names is complete.");
        convertNamesToCBID = config.getBoolean("convert-names-to-cbids", true);

        super.load();
    }
}