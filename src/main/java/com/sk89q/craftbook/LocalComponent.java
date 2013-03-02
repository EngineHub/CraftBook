package com.sk89q.craftbook;


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
}
