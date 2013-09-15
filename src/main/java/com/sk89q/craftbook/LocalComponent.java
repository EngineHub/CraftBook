package com.sk89q.craftbook;

import java.util.List;

/**
 * Author: Turtle9598
 */
public interface LocalComponent {

    /**
     * Called to enable the component.
     */
    public void enable();

    /**
     * Called to unload and disable the component.
     */
    public void disable();

    /**
     * Gets a {@link List} of mechanics that are associated with this component.
     * 
     * @return The list of mechanics.
     */
    public List<CraftBookMechanic> getMechanics();
}