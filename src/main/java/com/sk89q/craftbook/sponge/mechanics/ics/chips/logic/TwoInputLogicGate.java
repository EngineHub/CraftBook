package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import org.spongepowered.api.world.Location;

public abstract class TwoInputLogicGate extends IC {

    public TwoInputLogicGate(ICType<? extends IC> type, Location block) {
        super(type, block);
    }

    @Override
    public void trigger() {

        Boolean a = null;
        Boolean b = null;

        // New input handling: any/first two valid inputs discovered. Moar flexibility!
        for (int i = 0; i < getPinSet().getInputCount(); i++) {
            if (getPinSet().isValid(i, this)) {
                boolean pinval = getPinSet().getInput(i, this);
                // Got pin value, assign to first free variable, break if got both.
                if (a == null) {
                    a = pinval;
                } else if (b == null) {
                    b = pinval;
                } else {
                    break;
                }
            }
        }

        if (a == null || b == null) return;

        getPinSet().setOutput(0, getResult(a, b), this);
    }

    protected abstract boolean getResult(boolean a, boolean b);
}
