/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package com.sk89q.craftbook.mechanics.boat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({Boat.class, VehicleDestroyEvent.class, Drops.class})
public class BoatDropsTest {

    @Test
    public void testOnVehicleDestroy() {

        VehicleDestroyEvent event = mock(VehicleDestroyEvent.class);
        Boat boat = mock(Boat.class);
        Location loc = mock(Location.class);
        World world = mock(World.class);

        when(loc.getWorld()).thenReturn(world);
        when(boat.getLocation()).thenReturn(loc);

        when(event.getVehicle()).thenReturn(boat);
        when(event.getAttacker()).thenReturn(null);

        new Drops().onVehicleDestroy(event);

        verify(event).setCancelled(true);
        verify(boat).remove();
        verify(world).dropItemNaturally(org.mockito.Mockito.eq(loc), org.mockito.Mockito.any());
    }
}