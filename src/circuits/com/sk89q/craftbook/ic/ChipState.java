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

import java.util.*;

/**
 * <p>
 * Represents a chip state. Chip states provide information about pin inputs and
 * outputs.
 * </p>
 * 
 * <p>
 * Chip states keep state about pins, and have methods for mutating this state;
 * however, note that this does NOT translate into immediate changes in the
 * World. The chip state must still be applied to the World in order for changes
 * to take place.
 * </p>
 * 
 * @author sk89q
 * @author sturmeh
 */
public interface ChipState {
    /**
     * @param pin
     * @return the value at a pin.
     */
    public boolean get(int pin);
    
    /**
     * Set a pin's value.
     * 
     * @param pin
     * @param value
     */
    public void set(int pin, boolean value);
    
    
    
    
    
    public static class Basic implements ChipState {
        public Basic(BitSet bs, int size) {
            super();
            this.bs = bs;
            this.size = size;
        }
        
        private final BitSet bs;
        private final int size;
        
        public boolean get(int pin) {
            return bs.get(pin);
        }
        
        public void set(int pin, boolean value) {
            if (pin > size) return;
            bs.set(pin, value);
        }
        
    }
}
