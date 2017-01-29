package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WorldTimeModulus extends IC {

    public WorldTimeModulus(ICFactory<WorldTimeModulus> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            getPinSet().setOutput(0, getBlock().getExtent().getProperties().getWorldTime() % 2 == 1, this);
        }
    }

    public static class Factory extends ICFactory<WorldTimeModulus> {

        @Override
        public WorldTimeModulus createInstance(Location<World> location) {
            return new WorldTimeModulus(this, location);
        }
    }
}
