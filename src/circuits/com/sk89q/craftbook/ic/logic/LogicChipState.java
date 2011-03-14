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
 * FIXME ALL OF THIS IS CRAP. THIS SHOULD BE RE-DONE TO WRAP A ChipState.Basic
 * AND DO INDEX SHIFTING. This will allow PinPositionMap to work for
 * LogicChipState transparently.
 * 
 * @author Shaun (sturmeh)
 * @author sk89q
 * @author Lymia
 */
public class LogicChipState {   // implements ChipState?
    /**
     * Construct the state.  
     * 
     * @param in
     * @param out
     */
    protected LogicChipState(boolean[] in, boolean[] out) {
        this.in = in;
        this.out = out;
        mem = Arrays.copyOf(out, out.length);
    }
    
    /** input wire states */
    private boolean[] in;
    
    /** output wire states */
    private boolean[] out;
    
    /** old output wire states.  used to tell if we need to fire updates to the world or not after a change in input.
     *  it is essential to keep this memory instead of re-checking the world because even if the out-wires are at full power we don't know if it's because of us or not. */
    //         but this might not be the right place to do it, either.
    private boolean[] mem;
    
    /** blorch */
    private boolean hasErrored = false;
    
    
    
    /**
     * @param n
     * @return the state of the n'th input.
     */
    public boolean getIn(int n) {
        return in[n - 1];
    }
    
    /**
     * @return an array of all input states.
     */
    public boolean[] getInputs() {
        return Arrays.copyOf(in, in.length);
    }
    
    /**
     * @param n
     * @return the state of the n'th output.
     */
    public boolean getOut(int n) {
        return out[n - 1];
    }
    
    /**
     * @return an array of all output states.
     */
    public boolean[] getOutputs() {
        return Arrays.copyOf(out, out.length);
    }
    
    /**
     * @param n
     * @return the previous state of the n'th output.
     */
    public boolean getLast(int n) {
        return mem[n - 1];
    }
    
    /**
     * @return true if any outputs have been updated.
     */
    public boolean isModified() {
        return Arrays.equals(out,mem);
    }
    
    /**
     * Triggers an IC error, disabling the IC from further processing.
     */
    public void triggerError() {
        hasErrored = true;
    }
    
    /**
     * @return true if the IC has encountered an error state; false otherwise.
     */
    public boolean hasErrored() {
        return hasErrored;
    }
}
