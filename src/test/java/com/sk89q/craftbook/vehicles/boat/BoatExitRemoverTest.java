package com.sk89q.craftbook.vehicles.boat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Boat.class, VehicleExitEvent.class, ExitRemover.class})
public class BoatExitRemoverTest {

    @SuppressWarnings("serial")
    @Test
    public void testOnVehicleExit() {

        VehicleExitEvent event = mock(VehicleExitEvent.class);
        Boat boat = mock(Boat.class);

        Server server = mock(Server.class);
        when(server.getName()).thenReturn("Mock");
        when(server.getVersion()).thenReturn("MockVer");
        when(server.getBukkitVersion()).thenReturn("MockVer");
        when(server.getLogger()).thenReturn(Logger.getLogger(Logger.GLOBAL_LOGGER_NAME));
        when(server.getScheduler()).thenReturn(mock(BukkitScheduler.class));

        Bukkit.setServer(server);

        Location location = mock(Location.class);

        World world = mock(World.class);
        when(location.getWorld()).thenReturn(world);

        when(boat.getLocation()).thenReturn(location);

        when(event.getVehicle()).thenReturn(boat);

        ExitRemover rem = new ExitRemover();
        rem.onVehicleExit(event);

        verify(server).getScheduler();

        CraftBookPlugin plugin = mock(CraftBookPlugin.class);
        BukkitConfiguration config = mock(BukkitConfiguration.class);

        when(plugin.getConfiguration()).thenReturn(config);
        config.boatRemoveOnExitGiveItem = true;

        CraftBookPlugin.setInstance(plugin);

        rem.new BoatRemover(event).run();

        LivingEntity player = mock(LivingEntity.class);

        when(player.getLocation()).thenReturn(location);
        when(event.getExited()).thenReturn(player);

        rem.new BoatRemover(event).run();

        player = mock(Player.class);

        when(player.getLocation()).thenReturn(location);
        when(event.getExited()).thenReturn(player);

        Inventory inv = mock(PlayerInventory.class);

        when(inv.addItem(Mockito.<ItemStack[]>any())).thenReturn(new HashMap<Integer, ItemStack>(){{put(0,null);}});

        when(((Player) player).getInventory()).thenReturn((PlayerInventory) inv);

        rem.new BoatRemover(event).run();

        when(inv.addItem(Mockito.<ItemStack[]>any())).thenReturn(new HashMap<Integer, ItemStack>());
        rem.new BoatRemover(event).run();

        verify(boat, Mockito.times(4)).remove();
        verify(world, Mockito.times(3)).dropItemNaturally(Mockito.eq(location), Mockito.<ItemStack>any());
    }
}