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

    /**
     * Construct the state.
     * 
     * @param pos
     * @param blockPos
     * @param in
     * @param out
     * @param text
     */
    public ChipState(Vector pos, Vector blockPos, Signal[] in, Signal[] out, SignText text) {
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
}
