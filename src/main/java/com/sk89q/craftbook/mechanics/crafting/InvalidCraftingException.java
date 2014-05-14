package com.sk89q.craftbook.mechanics.crafting;

import com.sk89q.craftbook.util.exceptions.CraftbookException;

public class InvalidCraftingException extends CraftbookException {

    /**
     * 
     */
    private static final long serialVersionUID = 4305166656444438242L;

    public InvalidCraftingException(String message) {
        super(message);
    }
}