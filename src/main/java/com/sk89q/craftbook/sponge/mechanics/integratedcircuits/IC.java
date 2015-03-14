package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import org.spongepowered.api.block.BlockLoc;

public abstract class IC {

    public abstract BlockLoc getBlock();

    public String getDefaultPinSet() {
        return "SISO";
    }
}
