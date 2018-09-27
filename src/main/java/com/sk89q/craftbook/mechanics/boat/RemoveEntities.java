package com.sk89q.craftbook.mechanics.boat;

import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class RemoveEntities extends AbstractCraftBookMechanic {

    private static final Vector HALF_BLOCK_UP = new Vector(0, 0.5, 0);

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat))
            return;
        if (!removeOtherBoats && (event.getEntity() instanceof Boat || event.getEntity().isInsideVehicle()))
            return;

        if (event.getVehicle().isEmpty())
            return;

        if (event.getEntity() instanceof LivingEntity) {
            if(event.getEntity().isInsideVehicle())
                return;
            ((LivingEntity) event.getEntity()).damage(10);
            try {
                event.getEntity().setVelocity(event.getVehicle().getVelocity().normalize().multiply(1.8).add(HALF_BLOCK_UP));
            } catch(IllegalArgumentException e) {
                event.getEntity().setVelocity(HALF_BLOCK_UP);
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

    private boolean removeOtherBoats;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "remove-other-boats", "Allows the remove entities boats to remove other boats.");
        removeOtherBoats = config.getBoolean(path + "remove-other-boats", false);
    }
}