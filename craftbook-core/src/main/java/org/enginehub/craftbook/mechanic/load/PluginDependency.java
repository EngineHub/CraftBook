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

package com.sk89q.craftbook.mechanic.load;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.craftbook.CraftBook;

/**
 * A load dependency on another plugin.
 *
 * <p>
 *     This may vary based on the implementing platform.
 * </p>
 */
public class PluginDependency implements LoadDependency {

    private final String pluginName;
    private final boolean optional;

    public PluginDependency(String pluginName) {
        this(pluginName, false);
    }

    public PluginDependency(String pluginName, boolean optional) {
        checkNotNull(pluginName);

        this.pluginName = pluginName;
        this.optional = optional;
    }

    @Override
    public String getDependencyId() {
        return this.pluginName;
    }

    @Override
    public boolean isOptional() {
        return this.optional;
    }

    @Override
    public boolean isMet() {
        return CraftBook.getInstance().getPlatform().isPluginAvailable(this.pluginName);
    }
}
