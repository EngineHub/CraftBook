// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.ic;

/**
 * Represents a chip state. Chip states provide information about pin inputs and outputs.
 *
 * @author sk89q
 * @author sturmeh
 */
public interface ChipState {

    /**
     * Gets the value at a pin.
     *
     * @param pin
     *
     * @return
     */
    boolean get(int pin);

    /**
     * Gets the value for an input.
     * 
     * @param inputIndex 0-indexed number
     * 
     * @return
     */
    boolean getInput(int inputIndex);

    /**
     * Gets the value for an output.
     * 
     * @param outputIndex 0-indexed number
     * 
     * @return
     */
    boolean getOutput(int outputIndex);

    /**
     * Set a pin's value.
     *
     * @param pin
     * @param value
     */
    void set(int pin, boolean value);

    /*
     * Sets the value for an output.
     * 
     * @param outputIndex 0-indexed number
     * 
     * @return
     */
    void setOutput(int outputIndex, boolean value);

    /**
     * Returns whether this pin was triggered.
     *
     * @param pin
     *
     * @return
     */
    boolean isTriggered(int pin);

    /**
     * Returns whether this pin is connected and valid
     *
     * @param pin
     *
     * @return
     */
    boolean isValid(int pin);

    /**
     * Get the number of inputs.
     *
     * @return
     */
    int getInputCount();

    /**
     * Get the number of outputs.
     *
     * @return
     */
    int getOutputCount();
}
