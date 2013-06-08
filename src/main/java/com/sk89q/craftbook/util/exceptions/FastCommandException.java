package com.sk89q.craftbook.util.exceptions;

import com.sk89q.minecraft.util.commands.CommandException;

public class FastCommandException extends CommandException {

    /**
     * 
     */
    private static final long serialVersionUID = 3320261678457961218L;

    public FastCommandException(String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}