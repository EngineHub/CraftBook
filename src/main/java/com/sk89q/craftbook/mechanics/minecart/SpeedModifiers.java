package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;

public class SpeedModifiers extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail > 0)
            ((Minecart) event.getVehicle()).setDerailedVelocityMod(new Vector(CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail, CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail, CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierOffRail));
        if(CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierMaxSpeed != 1)
            ((Minecart) event.getVehicle()).setMaxSpeed(((Minecart) event.getVehicle()).getMaxSpeed() * CraftBookPlugin.inst().getConfiguration().minecartSpeedModifierMaxSpeed);
    }
}