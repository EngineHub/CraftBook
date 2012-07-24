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

package com.sk89q.craftbook.mech.ic;

/**
 * Carries a binary signal.
 *
 * @author Shaun (sturmeh)
 * @author sk89q
 */
public class Signal {

    private boolean state;
    private boolean triggered;

    /**
     * Constructor for new Signal
     *
     * @param state     initial state
     * @param triggered whether the state was triggered
     */
    public Signal(boolean state, boolean triggered) {

        this.state = state;
        this.triggered = triggered;
    }

    /**
     * Constructor for new Signal
     *
     * @param state initial state
     */
    public Signal(boolean state) {

        this.state = state;
        this.triggered = true;
    }

    /**
     * Setter for state.
     *
     * @param state state to set.
     */
    public void set(boolean state) {

        this.state = state;
    }

    /**
     * Whether the input was triggered.
     */
    public boolean isTriggered() {

        return triggered;
    }

    /**
     * Returns the current state.
     *
     * @return current state
     */
    public boolean is() {

        return state;
    }

    /**
     * Inverts the state.
     *
     * @return the new state.
     */
    public boolean invert() {

        state = !state;
        return state;
    }

    /**
     * Returns an inverted state.
     * Does not modify the state.
     *
     * @return the inverted state.
     */
    public boolean not() {

        return !state;
    }

    /**
     * Returns the string representation.
     *
     * @return 1 or 0.
     */
    public String text() {

        if (state) {
            return "OUTPUT: 1";
        }
        return "OUTPUT: 0";
    }
}
