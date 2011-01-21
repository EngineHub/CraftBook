// $Id$
/*
 * SKMinecraft
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
*/

package com.sk89q.craftbook;

/**
 *
 * @author sk89q
 */
public class InsufficientArgumentsException extends RuntimeException {
    private static final long serialVersionUID = 1213487587583511532L;

    public InsufficientArgumentsException(String error) {
        super(error);
    }
}
