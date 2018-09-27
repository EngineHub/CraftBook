package com.sk89q.craftbook.mechanics.boat;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class EmptyDecay extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof Boat)) return;

        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Decay((Boat) vehicle), delay);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        for (Entity ent : event.getChunk().getEntities()) {
            if (ent == null || !ent.isValid())
                continue;
            if (!(ent instanceof Boat))
                continue;
            if (!ent.isEmpty())
                continue;
            CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Decay((Boat) ent), delay);
        }
    }

    private static class Decay implements Runnable {

        Boat cart;

        public Decay(Boat cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if (cart == null || !cart.isValid() || !cart.isEmpty()) return;
            cart.remove();
        }
    }

    private int delay;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "time-in-ticks", "The time in ticks that the boat will wait before decaying.");
        delay = config.getInt(path + "time-in-ticks", 20);
    }
}