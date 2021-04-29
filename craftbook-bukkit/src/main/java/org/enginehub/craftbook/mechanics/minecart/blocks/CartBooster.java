/*
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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil.Power;

public class CartBooster extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        Vector newVelocity = event.getVehicle().getVelocity();

        if (event.getBlocks().matches(minecartSpeedModMaxBoostBlock)) {
            newVelocity.normalize().multiply(event.getMinecart().getMaxSpeed());
        } else if (event.getBlocks().matches(minecartSpeedMod25xBoostBlock))
            newVelocity.multiply(1.25d);
        else if (event.getBlocks().matches(minecartSpeedMod20xSlowBlock))
            newVelocity.multiply(0.8d);
        else if (event.getBlocks().matches(minecartSpeedMod50xSlowBlock))
            newVelocity.multiply(0.5d);
        else
            return;

        // go
        event.getVehicle().setVelocity(newVelocity);
    }

    private BaseBlock minecartSpeedModMaxBoostBlock;
    private BaseBlock minecartSpeedMod25xBoostBlock;
    private BaseBlock minecartSpeedMod50xSlowBlock;
    private BaseBlock minecartSpeedMod20xSlowBlock;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("max-boost-block", "Sets the block that is the base of the max boost block.");
        minecartSpeedModMaxBoostBlock = BlockParser.getBlock(config.getString("max-boost-block", BlockTypes.GOLD_BLOCK.getId()), true);

        config.setComment("25x-boost-block", "Sets the block that is the base of the 25x boost block.");
        minecartSpeedMod25xBoostBlock = BlockParser.getBlock(config.getString("25x-boost-block", BlockTypes.GOLD_ORE.getId()), true);

        config.setComment("50x-slow-block", "Sets the block that is the base of the 50x slower block.");
        minecartSpeedMod50xSlowBlock = BlockParser.getBlock(config.getString("50x-slow-block", BlockTypes.SOUL_SAND.getId()), true);

        config.setComment("20x-slow-block", "Sets the block that is the base of the 20x slower block.");
        minecartSpeedMod20xSlowBlock = BlockParser.getBlock(config.getString("20x-slow-block", BlockTypes.GRAVEL.getId()), true);
    }
}