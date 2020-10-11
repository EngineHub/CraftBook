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

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.report.Unreported;

import java.util.List;

/**
 * A CraftBook implementation of a configuration.
 */
public abstract class YamlConfiguration {

    public List<String> enabledMechanics;

    public boolean noOpPermissions;
    public boolean indirectRedstone;
    public boolean useBlockDistance;
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

    public abstract void load();

    public abstract void save();
}
