package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class HalfSubtractor extends IC {

    public HalfSubtractor(HalfSubtractor.Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void trigger() {
        boolean B = getPinSet().getInput(1, this);
        boolean C = getPinSet().getInput(2, this);

        boolean S = B ^ C;
        boolean Bo = !B & C;

        getPinSet().setOutput(0, S, this);
        getPinSet().setOutput(1, Bo, this);
        getPinSet().setOutput(2, Bo, this);
    }

    public static class Factory implements ICFactory<HalfSubtractor> {

        @Override
        public HalfSubtractor createInstance(Location<World> location) {
            return new HalfSubtractor(this, location);
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
                            "Ignored",
                            "Starting Value",
                            "First Subtrahend",
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
