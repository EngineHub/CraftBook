package com.sk89q.craftbook.vehicles.boat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Boat.class, VehicleDestroyEvent.class, BoatUncrashable.class})
public class BoatUncrashableTest {

    @Test
    public void testOnVehicleDestroy() {

        VehicleDestroyEvent event = mock(VehicleDestroyEvent.class);
        Boat boat = mock(Boat.class);

        when(event.getVehicle()).thenReturn(boat);

        new BoatUncrashable().onVehicleDestroy(event);

        when(event.getAttacker()).thenReturn(mock(LivingEntity.class));
        new BoatUncrashable().onVehicleDestroy(event);

        verify(event).setCancelled(true);
        verify(boat).setVelocity(new Vector(0,0,0));
    }
}