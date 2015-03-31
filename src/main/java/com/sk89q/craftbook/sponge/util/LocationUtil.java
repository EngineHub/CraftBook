package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

public class LocationUtil {

    public static Direction getFacing(Location base, Location other) {

        for (Direction dir : Direction.values()) {

            if (base.getRelative(dir).getPosition().equals(other.getPosition())) return dir;
        }

        return null;
    }
}
