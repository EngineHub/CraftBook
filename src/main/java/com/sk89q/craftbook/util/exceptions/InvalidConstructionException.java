package com.sk89q.craftbook.util.exceptions;

/**
 * Thrown when the mechanic type is not constructed correctly.
 */
public class InvalidConstructionException extends InvalidMechanismException {

    private static final long serialVersionUID = 4943494589521864491L;

    /**
     * Construct the object.
     *
     * @param msg
     */
    public InvalidConstructionException(String msg) {

        super(msg);
    }
}