package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.util.Direction;

public class LocationUtil {

    public static Direction getFacing(BlockLoc base, BlockLoc other) {

        for(Direction dir : Direction.values()) {

            if(base.getRelative(dir).equals(other))
                return dir;
        }

        return null;
    }
}
