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

package org.enginehub.craftbook.mechanics.ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @deprecated Use PersistentStorage instead in constructor/unload methods of IC Factories.
 */
@Deprecated
public interface PersistentDataIC {

    /**
     * Called when the {@link ICFactory} should load any persistant data required.
     *
     * @param stream The {@link DataInputStream} for the file that is being read from.
     * @throws IOException
     */
    void loadPersistentData(DataInputStream stream) throws IOException;

    /**
     * Called when the {@link ICFactory} should save any persistent data required.
     *
     * @param stream The {@link DataOutputStream} for the file that is being written to.
     * @throws IOException
     */
    void savePersistentData(DataOutputStream stream) throws IOException;

    /**
     * Gets the {@link File} in which the data should be saved/loaded.
     *
     * @return The {@link File} to save and load from.
     */
    File getStorageFile();
}