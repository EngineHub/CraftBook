package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class XorGate extends TwoInputLogicGate {

    public XorGate(ICType<? extends IC> type, Location<World> block) {
        super(type, block);
    }

    @Override
    boolean getResult(boolean a, boolean b) {
        return a != b;
    }
}
