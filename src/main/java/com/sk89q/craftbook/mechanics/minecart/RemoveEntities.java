package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
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

        if (!players && event.getEntity() instanceof Player) {
            return;
        }

        if(event.getVehicle() instanceof RideableMinecart && event.getVehicle().isEmpty() && !empty)
            return;

        if (event.getEntity() instanceof LivingEntity) {
            if(event.getEntity().isInsideVehicle())
                return;
            ((LivingEntity) event.getEntity()).damage(10);
            Vector newVelocity = event.getVehicle().getVelocity().normalize().multiply(1.8).add(new Vector(0,0.5,0));
            if (Double.isFinite(newVelocity.getX()) && Double.isFinite(newVelocity.getY()) && Double.isFinite(newVelocity.getZ())) {
                event.getEntity().setVelocity(newVelocity);
            }
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

    private boolean otherCarts;
    private boolean empty;
    private boolean players;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "remove-other-minecarts", "Allows the remove entities mechanic to remove other minecarts.");
        otherCarts = config.getBoolean(path + "remove-other-minecarts", false);

        config.setComment(path + "allow-empty-carts", "Allows the cart to be empty.");
        empty = config.getBoolean(path + "allow-empty-carts", false);

        config.setComment(path + "damage-players", "Allows the cart to damage and kill players.");
        players = config.getBoolean(path + "damage-players", true);
    }
}