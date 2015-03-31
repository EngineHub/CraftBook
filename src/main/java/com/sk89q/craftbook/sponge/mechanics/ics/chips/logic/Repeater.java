package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;

public class Repeater extends IC {

    public Repeater(ICType<IC> type, Location block) {
        super(type, block);
    }

    @Override
    public void trigger() {

        for (int i = 0; i < getPinSet().getInputCount(); i++)
            getPinSet().setOutput(i, getPinSet().getInput(i, this), this);
    }
}
