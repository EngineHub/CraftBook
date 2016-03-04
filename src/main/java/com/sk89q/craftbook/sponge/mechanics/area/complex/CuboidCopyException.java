package com.sk89q.craftbook.sponge.mechanics.area.complex;

public class CuboidCopyException extends Exception {

    private static final long serialVersionUID = 1610836109309177856L;

    /**
     * Construct an instance.
     */
    public CuboidCopyException() {

        super();
    }

    /**
     * Construct an instance.
     *
     * @param msg The message
     */
    public CuboidCopyException(String msg) {

        super(msg);
    }
}