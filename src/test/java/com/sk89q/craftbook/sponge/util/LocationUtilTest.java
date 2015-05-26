package com.sk89q.craftbook.sponge.util;

import org.junit.Test;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LocationUtilTest {

    @Test
    public void getFacingTest() {

        for(Direction direction : LocationUtil.getDirectFaces()) {
            Location loc = new Location(mock(Extent.class), 0, 0, 0);
            assertTrue(LocationUtil.getFacing(loc, loc.getRelative(direction)) == direction);
        }
    }
}
