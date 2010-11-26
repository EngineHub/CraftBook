package com.sk89q.craftbook.ic;

public class Signal {
	private boolean state;
	
	/**
    * Constructor for new Signal
    * 
    * @param state - initial state.
    */
	public Signal(boolean state) {
		this.set(state);
	}
	
	/**
    * Setter for state.
    * 
    * @param state - state to set.
    */
	public void set(boolean state) {
		this.state = state;
	}
	
	/**
    * Returns the current state.
    * 
    * @return Current state.
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
		if (state)
			return "OUTPUT: 1";
		return "OUTPUT: 0";
	}
}
