package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class FullSubtractor extends IC {

    public FullSubtractor(FullSubtractor.Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void trigger() {
        boolean A = getPinSet().getInput(0, this);
        boolean B = getPinSet().getInput(1, this);
        boolean C = getPinSet().getInput(2, this);

        boolean S = A ^ B ^ C;
        boolean Bo = C & A == B | !A & B;

        getPinSet().setOutput(0, S, this);
        getPinSet().setOutput(1, Bo, this);
        getPinSet().setOutput(2, Bo, this);
    }

    public static class Factory implements ICFactory<FullSubtractor> {

        @Override
        public FullSubtractor createInstance(Location<World> location) {
            return new FullSubtractor(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "",
                    ""
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Starting Value",
                            "First Subtrahend",
                            "Second Subtrahend"
                    },
                    new String[] {
                            "Difference",
                            "Borrow",
                            "Borrow (Same as Output 2)"
                    }
            };
        }
    }
}
