package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import org.spongepowered.api.world.Location;

public abstract class AnyInputLogicGate extends IC {

    public AnyInputLogicGate(ICType<? extends IC> type, Location block) {
        super(type, block);
    }

    @Override
    public void trigger() {
        short on = 0, valid = 0;
        for (short i = 0; i < getPinSet().getInputCount(); i++) {
            if (getPinSet().isValid(i, this)) {
                valid++;

                if (getPinSet().getInput(i, this)) {
                    on++;
                }
            }
        }

        // Condition; all valid must be ON, at least one valid.
        getPinSet().setOutput(0, getResult(valid, on), this);
    }

    public abstract boolean getResult(int wires, int on);
}
