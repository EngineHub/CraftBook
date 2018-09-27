package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;

public class CartMaxSpeed extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        double maxSpeed = 0.4D;
        try {
            maxSpeed = Double.parseDouble(event.getBlocks().getSign().getLine(2));
        } catch(Exception e){}

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        ((Minecart)event.getVehicle()).setMaxSpeed(maxSpeed);
    }

    @Override
    public boolean verify(ChangedSign sign, CraftBookPlayer player) {

        try {
            if(!sign.getLine(2).isEmpty())
                Double.parseDouble(sign.getLine(2));
        } catch (NumberFormatException e) {
            player.printError("Line 3 must be a number that represents the max speed!");
            return false;
        }
        return true;
    }

    @Override
    public String getName() {

        return "MaxSpeed";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[]{"Max Speed"};
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the max speed mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.COAL_BLOCK.getId()), true);
    }
}