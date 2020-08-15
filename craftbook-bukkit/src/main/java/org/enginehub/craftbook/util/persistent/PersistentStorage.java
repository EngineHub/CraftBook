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

package org.enginehub.craftbook.util.persistent;

import org.enginehub.craftbook.bukkit.CraftBookPlugin;

import java.util.Map;

/**
 * The base of the CraftBook persistant storage system. Used by mechanics to store data that is
 * needed persistently.
 */
public abstract class PersistentStorage {

    /**
     * Open (and load if neccesary) the connection.
     */
    public abstract void open();

    /**
     * Close (and save if neccesary) the connection.
     */
    public abstract void close();

    /**
     * Gets the type of this database.
     *
     * @return The type of database.
     */
    public abstract String getType();

    /**
     * Gets the data at the provided location.
     *
     * @param location A location, with '.'s seperating heirarchy.
     * @return The data.
     */
    public abstract Object get(String location);

    /**
     * Sets the data at the provided location.
     *
     * @param location The location to set the data at, with '.'s seperating heirarchy.
     * @param data The data to set.
     */
    public abstract void set(String location, Object data);

    /**
     * Returns whether the storage medium contains a value.
     *
     * @param location The location to check for data at, with '.'s seperating heirarchy.
     * @return Whether or not the data exists.
     */
    public abstract boolean has(String location);

    /**
     * Determines whether this storage method is usable.
     *
     * @return If it is usable.
     */
    public abstract boolean isValid();

    /**
     * Gets the current version of this database. Used for version conversions.
     *
     * @return The version of this database.
     */
    public abstract int getVersion();

    /**
     * Gets the current version that this version of CraftBook uses.
     *
     * @return The version CraftBook can read.
     */
    public abstract int getCurrentVersion();

    /**
     * Converts the database from one version to the next. Should work consecutively - converting up
     * a version each time until reaching latest.
     *
     * @param version The version to convert to. MUST be above previous version (Usually
     *     latest).
     */
    public abstract void convertVersion(int version);

    /**
     * Converts the database from one type to another, for example: YAML to SQL.
     *
     * @param type The database type to convert to.
     */
    public void convertType(String type) {

        Map<String, Object> data = exportData();
        PersistentStorage stor = createFromType(type);
        stor.importData(data, true);
        CraftBookPlugin.inst().setPersistentStorage(stor);
    }

    /**
     * Imports a {@link Map} of data into the {@link PersistentStorage} system.
     *
     * @param data The data to import.
     */
    public abstract void importData(Map<String, Object> data, boolean replace);

    /**
     * Export the data into a {@link Map}.
     *
     * @return The data in {@link Map} form.
     */
    public abstract Map<String, Object> exportData();

    /**
     * Generates a new PersistentStorage method from the type specified.
     *
     * @param type The type to create.
     * @return The new PersistentStorage.
     */
    public static PersistentStorage createFromType(String type) {

        if (type.equalsIgnoreCase("YAML"))
            return new YAMLPersistentStorage();
        else if (type.equalsIgnoreCase("DUMMY"))
            return new DummyPersistentStorage();
        else if (type.equalsIgnoreCase("SQLite"))
            return new SQLitePersistentStorage();
        else
            return null;
    }
}