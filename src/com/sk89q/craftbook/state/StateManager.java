package com.sk89q.craftbook.state;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateManager {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    private HashMap<String,StateHolder> stateHolders = 
        new HashMap<String,StateHolder>();
    
    public synchronized void addStateHolder(String name, StateHolder h) {
        stateHolders.put(name, h);
    }
    public synchronized void removeStateHolder(String name) {
        stateHolders.remove(name);
    }
    
    public synchronized void load(File target) { 
        for(String file: stateHolders.keySet()) {
            StateHolder h = stateHolders.get(file);
            try {
                h.read(new File(target,file));
            } catch (IOException e) {
                logger.logp(Level.SEVERE, "StateHolder", "load", "Failed to load state of '"+file+"'", e);
            }
        }
    }
    public synchronized void save(File target) {
        for(String file: stateHolders.keySet()) {
            StateHolder h = stateHolders.get(file);
            try {
                h.write(new File(target,file));
            } catch (IOException e) {
                logger.logp(Level.SEVERE, "StateHolder", "save", "Failed to save state of '"+file+"'", e);
            }
        }         
    }
} 
