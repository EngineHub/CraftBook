package com.sk89q.craftbook.sponge.mechanics.ics;

import org.spongepowered.api.world.Location;

public abstract class IC {

    ICType<? extends IC> type;
    Location block;
    boolean[] pinstates;

    public IC(ICType<? extends IC> type, Location block) {
        this.type = type;
        this.block = block;
    }

    public String getPinSetName() {
        return type.getDefaultPinSet();
    }

    public PinSet getPinSet() {
        return ICSocket.PINSETS.get(getPinSetName());
    }

    public void load() {

        PinSet set = getPinSet();
        pinstates = new boolean[set.getInputCount()]; // Just input for now.
    }

    public Location getBlock() {
        return block;
    }

    public ICType<? extends IC> getType() {
        return type;
    }

    public abstract void trigger();

    public boolean[] getPinStates() {
        return pinstates;
    }
}
