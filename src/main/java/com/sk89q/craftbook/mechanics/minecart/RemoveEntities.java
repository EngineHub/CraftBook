package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class RemoveEntities extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart))
            return;

        if (!otherCarts && (event.getEntity() instanceof Minecart || event.getEntity().isInsideVehicle()))
            return;

        if(event.getVehicle() instanceof RideableMinecart && event.getVehicle().isEmpty() && !empty)
            return;

        if (event.getEntity() instanceof LivingEntity) {
            if(event.getEntity().isInsideVehicle())
                return;
            ((LivingEntity) event.getEntity()).damage(10);
            event.getEntity().setVelocity(event.getVehicle().getVelocity().normalize().multiply(1.8).add(new Vector(0,0.5,0)));
        } else if (event.getEntity() instanceof Vehicle) {

            if(!event.getEntity().isEmpty())
                return;
            else
                event.getEntity().remove();
        } else
            event.getEntity().remove();

        event.setCancelled(true);
        event.setPickupCancelled(true);
        event.setCollisionCancelled(true);
    }

    boolean otherCarts;
    boolean empty;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment("vehicles.minecart.remove-entities.remove-other-minecarts", "Allows the remove entities mechanic to remove other minecarts.");
        otherCarts = config.getBoolean("vehicles.minecart.remove-entities.remove-other-minecarts", false);

        config.setComment("vehicles.minecart.remove-entities.allow-empty-carts", "Allows the cart to be empty.");
        empty = config.getBoolean("vehicles.minecart.remove-entities.allow-empty-carts", false);
    }
}