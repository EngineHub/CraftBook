// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.ic;

/**
 * Represents a chip state. Chip states provide information about pin
 * inputs and outputs.
 * 
 * @author sk89q
 * @author sturmeh
 */
public interface ChipState {
    
    /**
     * Gets the value at a pin.
     * 
     * @param pin
     * @return
     */
    public boolean get(int pin);
    
    /**
     * Set a pin's value.
     * 
     * @param pin
     * @param value
     */
    public void set(int pin, boolean value);
    
    /**
     * Returns whether this pin was triggered.
     * 
     * @param pin
     */
    //public void triggered(int pin);
    
}
