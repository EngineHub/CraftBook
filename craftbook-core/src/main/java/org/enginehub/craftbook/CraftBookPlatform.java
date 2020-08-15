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

package org.enginehub.craftbook;

import com.sk89q.worldedit.util.report.ReportList;
import org.enginehub.craftbook.mechanic.MechanicManager;
import org.enginehub.craftbook.util.profile.cache.ProfileCache;
import org.enginehub.craftbook.util.profile.resolver.ProfileService;
import org.enginehub.piston.CommandManager;

import java.nio.file.Path;

/**
 * A platform for implementing.
 */
public interface CraftBookPlatform {

    /**
     * Gets the name of the platform.
     *
     * @return The platform name
     */
    String getPlatformName();

    /**
     * Gets the version of the platform.
     *
     * @return The platform version
     */
    String getPlatformVersion();

    /**
     * Load the platform
     */
    void load();

    /**
     * Unload the platform
     */
    void unload();

    /**
     * Register the commands contained within the given command manager.
     *
     * @param commandManager the command manager
     */
    void registerCommands(CommandManager commandManager);

    /**
     * Gets the configuration directory.
     *
     * @return The config directory
     */
    Path getConfigDir();

    /**
     * Gets the Mechanic manager.
     *
     * @return The mechanic manager
     */
    MechanicManager getMechanicManager();

    /**
     * Get the global ConfigurationManager.
     * Use this to access global configuration values and per-world configuration values.
     *
     * @return The global ConfigurationManager
     */
    YamlConfiguration getConfiguration();

    /**
     * Adds reports specific to this platform.
     *
     * @param report The report list
     */
    void addPlatformReports(ReportList report);

    /**
     * Internal use.
     */
    ProfileService createProfileService(ProfileCache profileCache);

    /**
     * Gets whether this platform has a plugin by that name.
     *
     * @param pluginName The plugin name
     * @return Whether the plugin is available
     */
    boolean isPluginAvailable(String pluginName);
}
