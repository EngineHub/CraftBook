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
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SimplePowerable extends SpongeBlockMechanic {

    public abstract void updateState(Location<?> location, boolean powered);

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First BlockSnapshot source) {
        if(isValid(source.getLocation().get())) {
            boolean wasPowered = source.getLocation().get().get(CraftBookKeys.LAST_POWER).orElse(0) > 0;
            event.getNeighbors().entrySet().stream().map(
                    (Function<Map.Entry<Direction, BlockState>, Location>) entry -> source.getLocation().get().getRelative(entry.getKey())).
                    collect(Collectors.toList()).stream().filter((block) -> wasPowered != (BlockUtil.getBlockPowerLevel(source.getLocation().get(), block).orElse(0) > 0)).findFirst().ifPresent(block -> {
                boolean isPowered = BlockUtil.getBlockPowerLevel(source.getLocation().get(), block).orElse(0) > 0;
                updateState(source.getLocation().get(), isPowered);
                source.getLocation().get().offer(CraftBookKeys.LAST_POWER, isPowered ? 15 : 0);
            });
        }
    }
}
