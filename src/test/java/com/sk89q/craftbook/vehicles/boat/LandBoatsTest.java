package com.sk89q.craftbook.vehicles.boat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.bukkit.entity.Boat;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sk89q.craftbook.mechanics.boat.LandBoats;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Boat.class, VehicleCreateEvent.class, LandBoats.class})
public class LandBoatsTest {

    @Test
    public void testOnVehicleCreate() {

        VehicleCreateEvent event = mock(VehicleCreateEvent.class);
        Boat boat = mock(Boat.class);

        when(event.getVehicle()).thenReturn(boat);

        new LandBoats().onVehicleCreate(event);

        verify(boat).setWorkOnLand(true);
    }
}