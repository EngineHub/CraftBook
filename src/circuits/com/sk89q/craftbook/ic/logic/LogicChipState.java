// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Shaun (sturmeh)
 * Copyright (C) 2010 sk89q
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

package com.sk89q.craftbook.ic.logic;

import java.util.*;

import org.bukkit.block.*;

import com.sk89q.craftbook.ic.*;
import com.sk89q.worldedit.*;

/**
 * LogicChipState can be used to describe any IC that has clearly defined input
 * and output wires.
 * 
 * @author hash
 */
public interface LogicChipState extends ChipState {
    /**
     * @param n
     * @return the state of the n'th input.
     */
    public boolean getIn(int n);
    
    /**
     * @param n
     * @return the state of the n'th output.
     */
    public boolean getOut(int n);
    
    /**
     * @param n
     * @param value the state to set the n'th output to.
     */
    public void setOut(int n, boolean value);
    
    
    
    
    
    public static class FZISO implements LogicChipState {
        public FZISO(ChipState cs) {
            super();
            this.cs = cs;
        }
        
        private final ChipState cs;
        
        public boolean getIn(int n) {
            return get(n-1);
        }
        
        public boolean getOut(int n) {
            return false;
        }
        
        public void setOut(int n, boolean value) {
            ;
        }
        
        /** 
         * Delegates directly to the backing ChipState. Use of LogicChipState's
         * more specific {@link #getIn(int)} and {@link #getOut(int)} methods is
         * preferred.
         */
        public boolean get(int pin) {
            return cs.get(pin);
        }

        /**
         * Delegates directly to the backing ChipState. Use of LogicChipState's
         * more specific {@link #setOut(int, boolean)} method is preferred.
         */
        public void set(int pin, boolean value) {
            cs.set(pin, value);
        }
    }
    
    public static class FSISO implements LogicChipState {
        public FSISO(ChipState cs) {
            super();
            this.cs = cs;
        }
        
        private final ChipState cs;
        private static final int inputs = 1;
        
        public boolean getIn(int n) {
            return get(n-1);
        }
        
        public boolean getOut(int n) {
            return get(n-1+inputs);
        }
        
        public void setOut(int n, boolean value) {
            set(n-1+inputs, value);
        }
        
        /** 
         * Delegates directly to the backing ChipState. Use of LogicChipState's
         * more specific {@link #getIn(int)} and {@link #getOut(int)} methods is
         * preferred.
         */
        public boolean get(int pin) {
            return cs.get(pin);
        }

        /**
         * Delegates directly to the backing ChipState. Use of LogicChipState's
         * more specific {@link #setOut(int, boolean)} method is preferred.
         */
        public void set(int pin, boolean value) {
            cs.set(pin, value);
        }
    }
}
