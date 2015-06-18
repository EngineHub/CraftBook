package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import org.spongepowered.api.world.Location;

public class AndGate extends TwoInputLogicGate {

    public AndGate(ICType<? extends IC> type, Location block) {
        super(type, block);
    }

    @Override
    protected boolean getResult(boolean a, boolean b) {
        return a == b;
    }
}
