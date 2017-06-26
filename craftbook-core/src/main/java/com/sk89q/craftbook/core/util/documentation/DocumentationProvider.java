/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.core.util.documentation;

import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.PermissionNode;

public interface DocumentationProvider {

    ConfigValue<?>[] EMPTY_CONFIGURATION_NODES = new ConfigValue[0];
    PermissionNode[] EMPTY_PERMISSION_NODES = new PermissionNode[0];

    /**
     * Gets the relative path inside the docs directory that this
     * documentation file goes in.
     *
     * @return The path.
     */
    String getPath();

    /**
     * Gets the relative path inside the docs directory that this
     * documentation template file is in.
     *
     * @return The template path.
     */
    default String getTemplatePath() {
        return getPath();
    }

    /**
     * Allows documentation providers to provide their own content converters.
     *
     * @param input The input string.
     * @return The output string.
     */
    default String performCustomConversions(String input) {
        return input;
    }

    /**
     * Gets an array of all configuration nodes this mechanic uses.
     *
     * @return An array of configuration nodes.
     */
    default ConfigValue<?>[] getConfigurationNodes() {
        return EMPTY_CONFIGURATION_NODES;
    }

    /**
     * Gets an array of all permission nodes this mechanic uses.
     *
     * @return An array of all permission nodes.
     */
    default PermissionNode[] getPermissionNodes() {
        return EMPTY_PERMISSION_NODES;
    }
}
