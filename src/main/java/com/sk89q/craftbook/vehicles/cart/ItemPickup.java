package com.sk89q.craftbook.vehicles.cart;

import java.util.Collection;

import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;

public class ItemPickup extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getVehicle() instanceof StorageMinecart && event.getEntity() instanceof Item) {

            StorageMinecart cart = (StorageMinecart) event.getVehicle();
            Collection<ItemStack> leftovers = cart.getInventory().addItem(((Item) event.getEntity()).getItemStack()).values();
            if(leftovers.isEmpty())
                event.getEntity().remove();
            else
                ((Item) event.getEntity()).setItemStack(leftovers.toArray(new ItemStack[1])[0]);

            event.setCollisionCancelled(true);
            return;
        }
    }
}