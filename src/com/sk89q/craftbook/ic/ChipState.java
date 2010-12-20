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

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.*;

/**
 * Used to pass around the state of an IC.
 *
 * @author Shaun (sturmeh)
 * @author sk89q
 */
public class ChipState {
    private Signal[] in;
    private Signal[] out;
    private boolean[] mem;
    private Vector pos;
    private Vector blockPos;
    private SignText text;
    private boolean hasErrored = false;
    private long time;

    /**
     * Construct the state.
     * 
     * @param pos
     * @param blockPos
     * @param in
     * @param out
     * @param text
     */
    public ChipState(Vector pos, Vector blockPos, Signal[] in, Signal[] out, SignText text, long time) {
        this.pos = pos;
        this.blockPos = blockPos;
        this.in = in;
        this.out = out;
        this.text = text;

        mem = new boolean[out.length];
        int i = 0;
        for (Signal bit : out) {
            mem[i++] = bit.is();
        }
        
        this.time = time;
    }

    /**
     * Get an input state.
     * 
     * @param n
     * @return
     */
    public Signal getIn(int n) {
        if (n > in.length) {
            return null;
        }
        return in[n - 1];
    }
    /**
     * Gets all input states.
     */
    public Signal[] getInputs() {
        return in.clone();
    }

    /**
     * Get an output state.
     * 
     * @param n
     * @return
     */
    public Signal getOut(int n) {
        if (n > out.length) {
            return null;
        }
        return out[n - 1];
    }
    /**
     * Gets all output states.
     */
    public Signal[] getOutputs() {
        return out.clone();
    }

    /**
     * Returns the last state.
     * 
     * @param n
     * @return
     */
    public boolean getLast(int n) {
        if (n > mem.length) {
            return false;
        }
        return mem[n - 1];
    }

    /**
     * Returns whether any outputs have been updated.
     * 
     * @return
     */
    public boolean isModified() {
        int i = 0;

        for (Signal bit : out) {
            if (bit.is() != mem[i++]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the position.
     *
     * @return
     */
    public Vector getPosition() {
        return pos;
    }

    /**
     * Get the position of the IC block.
     *
     * @return
     */
    public Vector getBlockPosition() {
        return blockPos;
    }

    /**
     * Get the sign text.
     * 
     * @return
     */
    public SignText getText() {
        return text;
    }
    
    /**
     * Triggers an IC error, disabling the IC from further processing.
     */
    public void triggerError() {
    	hasErrored = true;
    }
    
    /**
     * Returns true if the IC has encountered an error state.
     * 
     * @return
     */
    public boolean hasErrored() {
    	return hasErrored;
    }
    
    public long getTime() {
        return time;
    }
}
