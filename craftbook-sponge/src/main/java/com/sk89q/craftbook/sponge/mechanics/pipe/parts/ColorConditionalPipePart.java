/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.pipe.parts;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ColorConditionalPipePart extends PassthroughPipePart {

    @Override
    public boolean isValid(BlockState blockState) {
        return blockState.getType() == BlockTypes.STAINED_GLASS;
    }

    @Override
    public boolean validateOutput(Location<World> location, Location<World> output, ItemStack itemStack) {
        if (output.get(Keys.DYE_COLOR).isPresent()) {
            return location.get(Keys.DYE_COLOR).orElse(DyeColors.WHITE).equals(output.get(Keys.DYE_COLOR).orElse(DyeColors.WHITE));
        }
        return super.validateOutput(location, output, itemStack);
    }
}
