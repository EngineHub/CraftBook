/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

    /**
     * Gets the relative path inside the mechanics directory that this
     * documentation file goes in.
     *
     * @return The path.
     */
    String getPath();

    /**
     * Gets the main documentation section.
     *
     * <p>
     *     Each string in the array refers to a different line.
     * </p>
     *
     * @return The main documentation section
     */
    String[] getMainDocumentation();

    /**
     * Gets an array of all configuration nodes this mechanic uses.
     *
     * @return An array of configuration nodes.
     */
    ConfigValue<?>[] getConfigurationNodes();

    /**
     * Gets an array of all permission nodes this mechanic uses.
     *
     * @return An array of all permission nodes.
     */
    PermissionNode[] getPermissionNodes();
}
