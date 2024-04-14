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

package org.enginehub.craftbook.mechanic.load;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.enginehub.craftbook.CraftBook;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A load dependency on another plugin.
 *
 * <p>
 * This may vary based on the implementing platform.
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

    @Override
    public Component getFailureMessage() {
        return TranslatableComponent.of(
            "craftbook.mechanisms.plugin-required",
            TextComponent.of(this.getDependencyId(), TextColor.WHITE)
        );
    }
}
