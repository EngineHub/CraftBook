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

    /**
     * Called when the mechanic should be disabled. This should make sure all memory is released.
     */
    public void disable();
}