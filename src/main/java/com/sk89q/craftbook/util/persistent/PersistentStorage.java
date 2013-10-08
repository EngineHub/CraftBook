package com.sk89q.craftbook.util.persistent;

/**
 * The base of the CraftBook persistant storage system. Used by mechanics to store data that is needed persistently.
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
     * Converts the database from one version to the next. Should work consecutively - converting up a version each time until reaching latest.
     *
     * @param version The version to convert to. MUST be above previous version (Usually latest).
     */
    public abstract void convertVersion(int version);

    /**
     * Converts the database from one type to another, for example: YAML to SQL.
     * 
     * @param type The database type to convert to.
     */
    public abstract void convertType(String type);
}