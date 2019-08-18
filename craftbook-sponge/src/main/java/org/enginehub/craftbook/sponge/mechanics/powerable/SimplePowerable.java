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
package org.enginehub.craftbook.sponge.mechanics.powerable;

import org.enginehub.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.enginehub.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

abstract class SimplePowerable extends SpongeBlockMechanic {

    public static final Location[] EMPTY_LOCATION_ARRAY = new Location[0];

    public abstract void updateState(Location<?> location, boolean powered);

    public abstract boolean getState(Location<?> location);

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First LocatableBlock source) {
        if(isValid(source.getLocation())) {
            boolean wasPowered = getState(source.getLocation());
            List<Location<World>> locations =  BlockUtil.getAdjacentExcept(source.getLocation(), Direction.NONE);
            locations.add(source.getLocation());
            Optional<Integer> power = BlockUtil.getDirectBlockPowerLevel(locations.toArray(EMPTY_LOCATION_ARRAY));
            power.ifPresent(integer -> {
                boolean isPowered = integer > 0;
                if (isPowered != wasPowered) {
                    updateState(source.getLocation(), isPowered);
                }
            });
        }
    }
}
