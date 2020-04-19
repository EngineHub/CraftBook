/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class CartBooster extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        Vector newVelocity = event.getVehicle().getVelocity();

        if(event.getBlocks().matches(minecartSpeedModMaxBoostBlock)) {
            newVelocity.normalize().multiply(event.getMinecart().getMaxSpeed());
        } else if(event.getBlocks().matches(minecartSpeedMod25xBoostBlock))
            newVelocity.multiply(1.25d);
        else if(event.getBlocks().matches(minecartSpeedMod20xSlowBlock))
            newVelocity.multiply(0.8d);
        else if(event.getBlocks().matches(minecartSpeedMod50xSlowBlock))
            newVelocity.multiply(0.5d);
        else
            return;

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

    private BlockStateHolder minecartSpeedModMaxBoostBlock;
    private BlockStateHolder minecartSpeedMod25xBoostBlock;
    private BlockStateHolder minecartSpeedMod50xSlowBlock;
    private BlockStateHolder minecartSpeedMod20xSlowBlock;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("max-boost-block", "Sets the block that is the base of the max boost block.");
        minecartSpeedModMaxBoostBlock = BlockSyntax.getBlock(config.getString("max-boost-block", BlockTypes.GOLD_BLOCK.getId()), true);

        config.setComment("25x-boost-block", "Sets the block that is the base of the 25x boost block.");
        minecartSpeedMod25xBoostBlock = BlockSyntax.getBlock(config.getString("25x-boost-block", BlockTypes.GOLD_ORE.getId()), true);

        config.setComment("50x-slow-block", "Sets the block that is the base of the 50x slower block.");
        minecartSpeedMod50xSlowBlock = BlockSyntax.getBlock(config.getString("50x-slow-block", BlockTypes.SOUL_SAND.getId()), true);

        config.setComment("20x-slow-block", "Sets the block that is the base of the 20x slower block.");
        minecartSpeedMod20xSlowBlock = BlockSyntax.getBlock(config.getString("20x-slow-block", BlockTypes.GRAVEL.getId()), true);
    }
}