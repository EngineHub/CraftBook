package com.sk89q.craftbook.ic;

public class Signal {

    private boolean state;
    private boolean triggered;

    /**
     * Constructor for new Signal
     *
     * @param state initial state
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
