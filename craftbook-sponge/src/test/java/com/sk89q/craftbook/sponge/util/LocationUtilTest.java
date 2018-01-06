/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
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
