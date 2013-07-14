package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class CartSpeedModifiers implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail > 0)
            ((Minecart) event.getVehicle()).setDerailedVelocityMod(new Vector(CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail, CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail, CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail));
        if(CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierMaxSpeed != 1)
            ((Minecart) event.getVehicle()).setMaxSpeed(((Minecart) event.getVehicle()).getMaxSpeed() * CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierMaxSpeed);
    }
}