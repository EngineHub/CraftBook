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

    private static Direction[] directFaces = null;

    public static Direction[] getDirectFaces() {

        if(directFaces == null)
            directFaces = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return directFaces;
    }
}
