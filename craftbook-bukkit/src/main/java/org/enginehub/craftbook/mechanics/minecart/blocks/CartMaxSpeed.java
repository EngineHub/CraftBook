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

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.event.EventHandler;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil.Power;

import java.util.List;

public class CartMaxSpeed extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.isMinor() || !event.getBlocks().matches(getBlock())) {
            return;
        }

        if (Power.OFF == isActive(event.getBlocks())) {
            return;
        }

        double maxSpeed = 0.4D;
        try {
            maxSpeed = Double.parseDouble(event.getBlocks().getChangedSign().getLine(2));
        } catch (Exception ignored) {
        }

        event.getMinecart().setMaxSpeed(maxSpeed);
    }

    @Override
    public boolean verify(ChangedSign sign, CraftBookPlayer player) {
        try {
            if (!sign.getLine(2).isEmpty()) {
                Double.parseDouble(sign.getLine(2));
            }
        } catch (NumberFormatException e) {
            player.printError("Line 3 must be a number that represents the max speed!");
            return false;
        }

        return true;
    }

    @Override
    public List<String> getApplicableSigns() {
        return ImmutableList.copyOf(new String[] { "MaxSpeed" });
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the max speed mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.COAL_BLOCK.getId()), true));
    }
}
