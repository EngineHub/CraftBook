package com.sk89q.craftbook.mechanics.boat;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

public class Drops extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleDestroy(VehicleDestroyEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        if (event.getAttacker() == null) {
            Boat boat = (Boat) event.getVehicle();
            boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(ItemUtil.getBoatFromTree(boat.getWoodType())));
            boat.remove();
            event.setCancelled(true);
        }
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}