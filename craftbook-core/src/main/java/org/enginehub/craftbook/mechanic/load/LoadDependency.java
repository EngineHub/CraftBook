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

public interface LoadDependency {

    /**
     * Get the ID of the depended item.
     *
     * @return The depended ID
     */
    String getDependencyId();

    /**
     * If the dependency is only optional rather than a
     * requirement.
     *
     * @return If it's optional
     */
    boolean isOptional();

    /**
     * Determines whether this dependency is currently met.
     *
     * @return if the dependency is met
     */
    boolean isMet();

    /**
     * The component to be displayed when the dependency is not met.
     *
     * @return The failure message
     */
    default Component getFailureMessage() {
        return TextComponent.of(getDependencyId());
    }
}
