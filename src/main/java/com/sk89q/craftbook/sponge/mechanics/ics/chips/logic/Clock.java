package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import com.sk89q.craftbook.sponge.mechanics.ics.SelfTriggeringIC;

public class Clock extends SelfTriggeringIC {

    public int ticks;

    public Clock(ICType<? extends IC> type, Location block) {
        super(type, block);
    }

    @Override
    public void think() {

        ticks++;
        if (ticks == 20) {
            ticks = 0;
            getPinSet().setOutput(0, !getPinSet().getOutput(0, this), this);
        }
    }

    @Override
    public boolean canThink() {
        return true;
    }
}
