package com.sk89q.craftbook.sponge.st;

import org.spongepowered.api.world.Location;

public interface SelfTriggeringMechanic {

    void onThink(Location location);
}
