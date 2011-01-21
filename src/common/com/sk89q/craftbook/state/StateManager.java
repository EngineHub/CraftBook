package com.sk89q.craftbook.state;

import java.util.HashMap;

public abstract class StateManager {
    protected HashMap<String,StateHolder> stateHolders = 
        new HashMap<String,StateHolder>();
    
    public synchronized void addStateHolder(String name, StateHolder h) {
        stateHolders.put(name, h);
    }
    public synchronized void removeStateHolder(String name) {
        stateHolders.remove(name);
    }   
} 
