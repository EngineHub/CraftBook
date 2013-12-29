package com.sk89q.craftbook.vehicles.boat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
        verify(world).dropItemNaturally(org.mockito.Mockito.eq(loc), org.mockito.Mockito.<ItemStack>any());
    }
}