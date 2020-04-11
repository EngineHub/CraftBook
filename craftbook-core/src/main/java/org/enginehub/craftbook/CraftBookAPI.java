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

import org.enginehub.craftbook.st.SelfTriggerManager;
import org.slf4j.Logger;

import java.io.File;
import java.util.Optional;

/**
 * The core class for all implementations of the CraftBook Core.
 */
public abstract class CraftBookAPI {

    private static CraftBookAPI instance;

    /**
     * Gets the current instance of the plugin.
     *
     * @param <T> The base plugin type.
     * @return The instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends CraftBookAPI> T inst() {
        return (T) instance;
    }

    public static <T extends CraftBookAPI> void setInstance(T api) {
        instance = api;
    }

    /**
     * Called to discover available mechanics.
     */
    public abstract void discoverMechanics();

    /**
     * Gets the {@link SelfTriggerManager}.
     *
     * @return The SelfTriggerManager.
     */
    public abstract Optional<SelfTriggerManager> getSelfTriggerManager();

    /**
     * Gets the working directory of CraftBook.
     *
     * @return The working directory
     */
    public abstract File getWorkingDirectory();

    /**
     * Gets the logger.
     *
     * @return The logger
     */
    public abstract Logger getLogger();

    /**
     * Gets the version as a string. This should be identifiable
     * with build information.
     *
     * @return The version string
     */
    public abstract String getVersionString();
}
