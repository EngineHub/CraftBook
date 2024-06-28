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

package org.enginehub.craftbook.mechanic;

import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;

import java.io.File;

/**
 * Represents a CraftBook Mechanic.
 */
public interface CraftBookMechanic {

    /**
     * Get the {@link MechanicType} instance that represents this mechanic.
     *
     * @return The mechanic type instance
     */
    MechanicType<? extends CraftBookMechanic> getMechanicType();

    /**
     * Called when a mechanic should be initialized. This includes creating of any maps, lists or
     * singleton instances.
     *
     * @throws MechanicInitializationException if the mechanic failed to initialise
     */
    default void enable() throws MechanicInitializationException {
    }

    /**
     * Called when the mechanic should be disabled. This should make sure all memory is released.
     */
    default void disable() {
    }

    /**
     * Called when a mechanic's configuration has been re-loaded.
     */
    default void reload() throws MechanicInitializationException {
        disable();
        enable();
    }

    /**
     * Load the configuration from file, and delegate
     * to the value loader.
     *
     * @param configFile The configuration file
     */
    void loadConfiguration(File configFile);

    /**
     * Load config values from the given YAMLProcessor.
     *
     * @param config The YAMLProcessor for this config.
     */
    default void loadFromConfiguration(YAMLProcessor config) {
    }

    default String getDocsUrl(MechanicType<? extends CraftBookMechanic> mechanicType) {
        return CraftBook.getDocsDomain() + "mechanics/" + mechanicType.id() + "/";
    }
}
