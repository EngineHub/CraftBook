package com.sk89q.craftbook;

import org.bukkit.event.Listener;

/**
 * Represents a CraftBook Mechanic.
 */
public interface CraftBookMechanic extends Listener {

    /**
     * Called when a mechanic should be initialized. This includes creating of any maps, lists or singleton instances.
     * 
     * @return if it enabled properly. Note: returning false will cause the mechanic to be disabled.
     */
    public boolean enable();

    public void disable();
}