/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.minecart.block;

import com.sk89q.craftbook.core.util.TernaryState;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class SpongeCartBlockMechanic extends SpongeSignMechanic {

    public TernaryState isActive(CartMechanismBlocks blocks) {
        boolean isConnected = false;
        for (Location<World> block : blocks.asList()) {
            switch (isActive(block)) {
                case TRUE:
                    return TernaryState.TRUE;
                case NONE:
                    break;
                case FALSE:
                    isConnected = true;
            }

        }
        return isConnected ? TernaryState.FALSE : TernaryState.NONE;
    }

    public TernaryState isActive(Location<World> block) {
        boolean isConnected = false;
        for (Direction direction : BlockUtil.getDirectHorizontalFaces()) {
            // TODO
        }

        return isConnected ? TernaryState.FALSE : TernaryState.NONE;
    }
}
