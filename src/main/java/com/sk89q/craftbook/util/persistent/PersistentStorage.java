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
}