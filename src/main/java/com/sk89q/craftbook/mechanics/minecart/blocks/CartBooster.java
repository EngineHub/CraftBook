package com.sk89q.craftbook.mechanics.minecart.blocks;

import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.util.yaml.YAMLProcessor;

public class CartBooster extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        double multiplier;

        if(event.getBlocks().matches(minecartSpeedModMaxBoostBlock))
            multiplier = 100;
        else if(event.getBlocks().matches(minecartSpeedMod25xBoostBlock))
            multiplier = 1.25;
        else if(event.getBlocks().matches(minecartSpeedMod20xSlowBlock))
            multiplier = 0.8;
        else if(event.getBlocks().matches(minecartSpeedMod50xSlowBlock))
            multiplier = 0.5;
        else
            return;

        // speed up or down
        Vector newVelocity;
        newVelocity = event.getVehicle().getVelocity().multiply(multiplier);
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

    private ItemInfo minecartSpeedModMaxBoostBlock;
    private ItemInfo minecartSpeedMod25xBoostBlock;
    private ItemInfo minecartSpeedMod50xSlowBlock;
    private ItemInfo minecartSpeedMod20xSlowBlock;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "max-boost-block", "Sets the block that is the base of the max boost block.");
        minecartSpeedModMaxBoostBlock = new ItemInfo(config.getString(path + "max-boost-block", "GOLD_BLOCK:0"));

        config.setComment(path + "25x-boost-block", "Sets the block that is the base of the 25x boost block.");
        minecartSpeedMod25xBoostBlock = new ItemInfo(config.getString(path + "25x-boost-block", "GOLD_ORE:0"));

        config.setComment(path + "50x-slow-block", "Sets the block that is the base of the 50x slower block.");
        minecartSpeedMod50xSlowBlock = new ItemInfo(config.getString(path + "50x-slow-block", "SOUL_SAND:0"));

        config.setComment(path + "20x-slow-block", "Sets the block that is the base of the 20x slower block.");
        minecartSpeedMod20xSlowBlock = new ItemInfo(config.getString(path + "20x-slow-block", "GRAVEL:0"));
    }
}