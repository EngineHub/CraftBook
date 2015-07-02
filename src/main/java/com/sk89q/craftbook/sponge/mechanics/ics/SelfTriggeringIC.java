package com.sk89q.craftbook.sponge.mechanics.ics;

import org.spongepowered.api.world.Location;

public abstract class SelfTriggeringIC extends IC {

    public boolean selfTriggering;

    public SelfTriggeringIC(ICType<? extends IC> type, Location block) {
        this.type = type;
        this.block = block;
    }

    public abstract void think();

    public boolean canThink() {
        return selfTriggering;
    }

    @Override
    public void trigger() {
    }
}
