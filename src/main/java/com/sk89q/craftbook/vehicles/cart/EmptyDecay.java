package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class EmptyDecay extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {

        Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof Minecart)) return;

        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Decay((Minecart) vehicle), CraftBookPlugin.inst().getConfiguration().minecartDecayTime);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {

        for (Entity ent : event.getChunk().getEntities()) {
            if (ent == null || ent.isDead())
                continue;
            if (!(ent instanceof Minecart))
                continue;
            if (!ent.isEmpty())
                continue;
            CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Decay((Minecart) ent), CraftBookPlugin.inst().getConfiguration().minecartDecayTime);
        }
    }

    static class Decay implements Runnable {

        Minecart cart;

        public Decay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if (cart != null && cart.isEmpty())
                cart.remove();
        }
    }
}