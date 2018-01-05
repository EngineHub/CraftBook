package com.sk89q.craftbook.sponge.util;

import static org.junit.Assert.assertEquals;

import com.flowpowered.math.vector.Vector3d;
import org.junit.Test;

public class LocationUtilTest {

    @Test
    public void testCartesianToEuler() {
        Vector3d cartesian = new Vector3d(1.0, 0.0, 0.0);
        Vector3d euler = LocationUtil.cartesianToEuler(cartesian);
        assertEquals(euler, new Vector3d(-0.0, 270.0, 0.0));

        cartesian = new Vector3d(1.0, 0.0, 1.0);
        euler = LocationUtil.cartesianToEuler(cartesian);
        assertEquals(euler, new Vector3d(-0.0, 315.0, 0.0));

        cartesian = new Vector3d(1.0, 0.0, -1.0);
        euler = LocationUtil.cartesianToEuler(cartesian);
        assertEquals(euler, new Vector3d(-0.0, 225.0, 0.0));

        cartesian = new Vector3d(1.0, -1.0, 0.0);
        euler = LocationUtil.cartesianToEuler(cartesian);
        assertEquals(euler, new Vector3d(45.0, 270.0, 0.0));
    }
}
