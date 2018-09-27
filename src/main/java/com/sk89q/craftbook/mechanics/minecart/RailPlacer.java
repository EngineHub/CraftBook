package com.sk89q.craftbook.mechanics.minecart;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Material;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

public class RailPlacer extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof StorageMinecart)) return;

        if(event.getTo().getBlock().getType() == Material.AIR
                && event.getTo().getBlock().getRelative(0, -1, 0).getType().isSolid()
                && ((StorageMinecart)event.getVehicle()).getInventory().contains(Material.RAIL)) {

            if(((StorageMinecart)event.getVehicle()).getInventory().removeItem(new ItemStack(Material.RAIL, 1)).isEmpty())
                event.getTo().getBlock().setType(Material.RAIL);
        }
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}