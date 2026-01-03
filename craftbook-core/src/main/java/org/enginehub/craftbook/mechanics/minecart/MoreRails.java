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

package org.enginehub.craftbook.mechanics.minecart;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;


public abstract class MoreRails extends AbstractCraftBookMechanic {

    public MoreRails(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    /**
     * Gets whether this block is a valid "MoreRails" rail block.
     *
     * @param blockType The block type to check
     * @return if it's valid
     */
    public boolean isValidRail(BlockType blockType) {
        return (ladder && (blockType == BlockTypes.LADDER || blockType == BlockTypes.VINE))
            || (pressurePlate && BlockCategories.PRESSURE_PLATES.contains(blockType));
    }

    public boolean ladder;
    protected double ladderVerticalVelocity;
    public boolean pressurePlate;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("pressure-plate-intersection", "Allows use of pressure plates as rail intersections.");
        pressurePlate = config.getBoolean("pressure-plate-intersection", true);

        config.setComment("ladder-vertical-rail", "Allows use of ladders and vines as a vertical rail.");
        ladder = config.getBoolean("ladder-vertical-rail", true);

        config.setComment("ladder-vertical-rail-velocity", "Sets the velocity applied to the minecart on vertical rails.");
        ladderVerticalVelocity = config.getDouble("ladder-vertical-rail-velocity", 0.1D);
    }
}
