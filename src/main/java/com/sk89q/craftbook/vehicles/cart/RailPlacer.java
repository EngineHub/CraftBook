package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

public class RailPlacer implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (event.getVehicle() instanceof StorageMinecart) {

            if(event.getTo().getBlock().getTypeId() == 0 && !BlockType.canPassThrough(event.getTo().getBlock().getRelative(0, -1, 0).getTypeId()) && ((StorageMinecart)event.getVehicle()).getInventory().contains(BlockID.MINECART_TRACKS)) {

                if(((StorageMinecart)event.getVehicle()).getInventory().removeItem(new ItemStack(BlockID.MINECART_TRACKS, 1)).isEmpty())
                    event.getTo().getBlock().setTypeId(BlockID.MINECART_TRACKS);
            }
        }
    }
}