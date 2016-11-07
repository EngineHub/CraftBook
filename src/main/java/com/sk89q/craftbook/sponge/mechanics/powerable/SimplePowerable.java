/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.powerable;

import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

abstract class SimplePowerable extends SpongeBlockMechanic {

    public static final Location[] EMPTY_LOCATION_ARRAY = new Location[0];

    public abstract void updateState(Location<?> location, boolean powered);

    public abstract boolean getState(Location<?> location);

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First BlockSnapshot source) {
        source.getLocation().ifPresent((location) -> {
            if(isValid(location)) {
                boolean wasPowered = getState(location);
                List<Location<World>> locations =  BlockUtil.getAdjacentExcept(location, Direction.NONE);
                locations.add(location);
                Optional<Integer> power = BlockUtil.getDirectBlockPowerLevel(locations.toArray(EMPTY_LOCATION_ARRAY));
                if (power.isPresent()) {
                    boolean isPowered = power.get() > 0;
                    if (isPowered != wasPowered) {
                        updateState(location, isPowered);
                    }
                }
            }
        });
    }
}
