/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.YamlConfiguration;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;

public class BukkitConfiguration extends YamlConfiguration {

    public BukkitConfiguration(YAMLProcessor config) {
        super(config);
    }

    @Override
    public void load() {
        try {
            config.load();
        } catch (IOException e) {
            CraftBook.logger.error("Error loading CraftBook configuration", e);
            e.printStackTrace();
        }

        if (config.getList("enabled-mechanics") != null) {
            try {
                Files.move(
                    CraftBook.getInstance().getPlatform().getConfigDir().resolve("config.yml"),
                    CraftBook.getInstance().getPlatform().getConfigDir().resolve("config.yml.old")
                );

                CraftBookPlugin.inst().createDefaultConfiguration("config.yml");

                config.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config.setWriteDefaults(true);

        config.setHeader(
            "# CraftBook Configuration for Bukkit.",
            "# This configuration will automatically add new configuration options for you,",
            "# So there is no need to regenerate this configuration unless you want to.",
            "# More information about these features are available at:",
            "# " + CraftBookPlugin.getDocsDomain() + "mechanics/",
            "#",
            "# NOTE! NOTHING IS ENABLED BY DEFAULT! ENABLE FEATURES TO USE THEM!",
            "");

        enabledMechanics = new ArrayList<>();
        config.setComment("mechanics", "List of mechanics and whether they are enabled or not");
        MechanicType.REGISTRY.values()
            .stream()
            .sorted(Comparator.comparing((MechanicType<?> t) -> t.getCategory().name()).thenComparing(MechanicType::getId))
            .forEach(mechanicType -> {
                String path = "mechanics." + mechanicType.getCategory().name().toLowerCase() + "." + mechanicType.getId();
                boolean enabled = config.getBoolean(path, mechanicType.getId().equals("variables"));
                if (enabled) {
                    enabledMechanics.add(mechanicType.getId());
                }
            });

        config.setComment("st-think-ticks", "WARNING! Changing this can result in all ST mechanics acting very weirdly, only change this if you know what you are doing!");
        stThinkRate = config.getInt("st-think-ticks", 2);

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

        config.setComment("sign-click-timeout", "How often in milliseconds players can interact with CraftBook signs.");
        signClickTimeout = config.getInt("sign-click-timeout", 500);

        config.setComment("debug-mode", "Enable a mode that will print extra debug information to the console.");
        debugMode = config.getBoolean("debug-mode", false);

        config.setComment("debug-mode-file-logging", "Causes all debug mode output to be logged into a file. This file is reset every startup (And every /cb reload).");
        debugLogToFile = config.getBoolean("debug-mode-file-logging", false);

        config.setComment("debug-flags", "Enable certain debug types when debug mode is enabled.");
        debugFlags = config.getStringList("debug-flags", new ArrayList<>());

        config.setComment("show-permission-messages", "Show messages when a player does not have permission to do something.");
        showPermissionMessages = config.getBoolean("show-permission-messages", true);

        config.setComment("persistent-storage-type", "PersistentStorage stores data that can be accessed across server restart. Method of PersistentStorage storage (Note: DUMMY is practically off, and may cause issues). Can currently be any of the following: YAML, DUMMY, SQLite");
        persistentStorageType = config.getString("persistent-storage-type", "YAML");

        config.save();
    }

    @Override
    public void save() {
        for (MechanicType<?> availableMechanic : MechanicType.REGISTRY.values()) {
            String path = "mechanics." + availableMechanic.getCategory().name().toLowerCase() + "." + availableMechanic.getId();
            config.setProperty(path, enabledMechanics.contains(availableMechanic.getId()));
        }

        config.save();
    }
}
