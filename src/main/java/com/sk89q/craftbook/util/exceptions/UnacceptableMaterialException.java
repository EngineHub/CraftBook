package com.sk89q.craftbook.util.exceptions;

/**
 * Thrown when the mechanic type is unacceptable.
 */
public class UnacceptableMaterialException extends InvalidMechanismException {

    public UnacceptableMaterialException(String msg) {

        super(msg);
    }

    private static final long serialVersionUID = 8340723004466483212L;
}