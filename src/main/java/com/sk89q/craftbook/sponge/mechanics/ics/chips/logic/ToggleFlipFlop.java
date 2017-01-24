package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ToggleFlipFlop extends IC {

    private boolean risingEdge;

    public ToggleFlipFlop(ICFactory<ToggleFlipFlop> icFactory, Location<World> block, boolean risingEdge) {
        super(icFactory, block);

        this.risingEdge = risingEdge;
    }

    @Override
    public void trigger() {
        if (risingEdge == getPinSet().getInput(0, this)) {
            getPinSet().setOutput(0, !getPinSet().getOutput(0, this), this);
        }
    }

    public static class Factory extends ICFactory<ToggleFlipFlop> {

        private boolean risingEdge;

        public Factory(boolean risingEdge) {
            this.risingEdge = risingEdge;
        }

        @Override
        public ToggleFlipFlop createInstance(Location<World> location) {
            return new ToggleFlipFlop(this, location, risingEdge);
        }
    }
}
