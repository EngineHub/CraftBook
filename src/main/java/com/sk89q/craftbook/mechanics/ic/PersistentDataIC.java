package com.sk89q.craftbook.mechanics.ic;

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
     * 
     * @throws IOException
     */
    void loadPersistentData(DataInputStream stream) throws IOException;

    /**
     * Called when the {@link ICFactory} should save any persistent data required.
     * 
     * @param stream The {@link DataOutputStream} for the file that is being written to.
     * 
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