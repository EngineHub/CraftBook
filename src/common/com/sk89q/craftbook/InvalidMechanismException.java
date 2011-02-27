// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

/**
 * Thrown when a MechanicFactory is considering whether or not to produce a
 * Mechanic and finds that an area of the world looks like it it was intended to
 * be a mechanism, but it is is some way not a valid construction. (For example,
 * an area with a "[Bridge]" sign which has blocks of an inappropriate material,
 * or a sign facing an invalid direction, etc.) It is appropriate to extend this
 * exception to produce more specific types.
 * 
 * @author hash
 */
public class InvalidMechanismException extends CraftbookException {
    private static final long serialVersionUID = -6917162805444409894L;

    public InvalidMechanismException() {
        super();
    }
    
    public InvalidMechanismException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidMechanismException(String message) {
        super(message);
    }
    
    public InvalidMechanismException(Throwable cause) {
        super(cause);
    }
}
