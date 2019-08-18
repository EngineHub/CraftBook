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
package org.enginehub.craftbook.sponge.mechanics.pipe.parts;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public interface PipePart {

    boolean isValid(BlockState blockState);

    /**
     * Finds all locations that this pipe part is able to output items to.
     *
     * This does not need to check the validity of the target location block.
     *
     * @param location The location of this part.
     * @param itemStack The itemstack passed through.
     * @param inputSide The side that the input has come from.
     * @return A list of possible output locations.
     */
    List<Location<World>> findPotentialOutputs(Location<World> location, ItemStack itemStack, Direction inputSide);

    /**
     * Determines if an output is valid.
     *
     * <p>
     *     This only needs to check for special behaviour.
     * </p>
     *
     * @param location The location of this part.
     * @param output The location of the output.
     * @param itemStack The itemstack passed through.
     * @return If the output is valid.
     */
    default boolean validateOutput(Location<World> location, Location<World> output, ItemStack itemStack) {
        return true;
    }
}
