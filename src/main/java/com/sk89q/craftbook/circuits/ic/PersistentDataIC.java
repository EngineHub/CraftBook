package com.sk89q.craftbook.circuits.ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public interface PersistentDataIC {

    /**
     * Called when the IC should load any persistant data required.
     * @throws IOException
     */
    public void loadPersistentData(DataInputStream stream) throws IOException;

    /**
     * Called when the IC should save any persistent data required.
     * @throws IOException
     */
    public void savePersistentData(DataOutputStream stream) throws IOException;

    /**
     * Gets the file in which the data should be saved/loaded.
     * 
     * @return The file to save and load from.
     */
    public File getStorageFile();
}