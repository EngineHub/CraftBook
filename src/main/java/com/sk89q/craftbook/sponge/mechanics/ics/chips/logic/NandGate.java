package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class NandGate extends AnyInputLogicGate {

    public NandGate(ICType<? extends IC> type, Location<World> block) {
        super(type, block);
    }

    @Override
    public boolean getResult(int wires, int on) {
        return wires > 0 && on != wires;
    }
}
