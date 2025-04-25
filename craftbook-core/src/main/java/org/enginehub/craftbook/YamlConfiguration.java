/*
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

package org.enginehub.craftbook;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.report.Unreported;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A CraftBook implementation of a configuration.
 */
public abstract class YamlConfiguration {

    public List<String> enabledMechanics;

    public boolean noOpPermissions;
    public boolean indirectRedstone;
    public boolean safeDestruction;
    public int stThinkRate;
    public boolean obeyWorldGuard;
    public boolean obeyPluginProtections;
    public boolean showPermissionMessages;
    public long signClickTimeout;

    public boolean debugMode;
    public boolean debugLogToFile;
    public List<String> debugFlags;

    @Unreported
    public YAMLProcessor config;

    public YamlConfiguration(YAMLProcessor config) {
        this.config = config;
    }

    public void load() {
        config.setWriteDefaults(true);

        config.setHeader(
                "# CraftBook Configuration for " + CraftBook.getInstance().getPlatform().getPlatformName() + ".",
                "# This configuration will automatically add new configuration options for you,",
                "# So there is no need to regenerate this configuration unless you want to.",
                "# More information about these features are available at:",
                "# " + CraftBook.getDocsDomain() + "mechanics/",
                "#",
                "# NOTE! NOTHING IS ENABLED BY DEFAULT! ENABLE FEATURES TO USE THEM!",
                "");

        enabledMechanics = new ArrayList<>();
        config.setComment("mechanics", "List of mechanics and whether they are enabled or not");
        MechanicType.REGISTRY.values()
                .stream()
                .sorted(Comparator.comparing((MechanicType<?> t) -> t.getCategory().name()).thenComparing(MechanicType::id))
                .forEach(mechanicType -> {
                    String path = "mechanics." + mechanicType.getCategory().name().toLowerCase(Locale.ENGLISH) + "." + mechanicType.id();
                    boolean enabled = config.getBoolean(path, mechanicType.id().equals("variables"));
                    if (enabled) {
                        enabledMechanics.add(mechanicType.id());
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

        config.setComment("obey-worldguard-flags", "Whether WorldGuard flags should be checked when performing CraftBook actions.");
        obeyWorldGuard = config.getBoolean("obey-worldguard-flags", true);

        config.setComment("obey-plugin-protections", "Whether to obey other plugins attempts to cancel CraftBook actions.");
        obeyPluginProtections = config.getBoolean("obey-plugin-protections", true);

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

        config.save();
    }

    public void save() {
        for (MechanicType<?> availableMechanic : MechanicType.REGISTRY.values()) {
            String path = "mechanics." + availableMechanic.getCategory().name().toLowerCase(Locale.ENGLISH) + "." + availableMechanic.id();
            config.setProperty(path, enabledMechanics.contains(availableMechanic.id()));
        }

        config.save();
    }
}
