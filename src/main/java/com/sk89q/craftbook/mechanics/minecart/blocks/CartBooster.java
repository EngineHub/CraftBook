package com.sk89q.craftbook.mechanics.minecart.blocks;

import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RedstoneUtil.Power;

public class CartBooster extends CartBlockMechanism {

    public CartBooster(ItemInfo material, double multiplier) {

        super(material);
        this.multiplier = multiplier;
    }

    private final double multiplier;

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        // speed up or down
        Vector newVelocity;
        if (multiplier != 1) {
            newVelocity = event.getVehicle().getVelocity().multiply(multiplier);
        } else return;
        // go
        event.getVehicle().setVelocity(newVelocity);
    }

    @Override
    public String getName() {

        return "Booster";
    }

    @Override
    public String[] getApplicableSigns() {

        return null;
    }
}