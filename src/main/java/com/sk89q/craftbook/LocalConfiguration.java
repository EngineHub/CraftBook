package com.sk89q.craftbook;

/**
 * A implementation of Configuration based off of {@link com.sk89q.worldedit.LocalConfiguration} for CraftBook.
 */
public abstract class LocalConfiguration {

    /**
     * Loads the configuration.
     */
    public abstract void load();
}