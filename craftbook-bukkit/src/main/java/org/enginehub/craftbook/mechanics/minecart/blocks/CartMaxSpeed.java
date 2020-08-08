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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockSyntax;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
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
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the max speed mechanic.");
        material = BlockSyntax.getBlock(config.getString("block", BlockTypes.COAL_BLOCK.getId()), true);
    }
}