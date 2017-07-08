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

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.craftbook.core.util.TernaryState;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

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
                    break;
            }

        }
        return isConnected ? TernaryState.FALSE : TernaryState.NONE;
    }

    public TernaryState isActive(Location<World> block) {
        boolean isConnected = false;
        for (Direction direction : BlockUtil.getDirectHorizontalFaces()) {
            switch (BlockUtil.isPowered(block, direction)) {
                case TRUE:
                    return TernaryState.TRUE;
                case NONE:
                    break;
                case FALSE:
                    isConnected = true;
                    break;
            }
        }

        return isConnected ? TernaryState.FALSE : TernaryState.NONE;
    }

    @Listener
    public void onVehicleMove(MoveEntityEvent event, @Getter("getTargetEntity") Minecart minecart) {
        if (event.getFromTransform().getPosition().distanceSquared(event.getToTransform().getPosition()) > 2 * 2) {
            return;
        }
        CartMechanismBlocks.findByRail(minecart.getLocation()).filter(this::isValid).ifPresent(cartMechanismBlocks -> {
            Vector3d from = event.getFromTransform().getPosition();
            Vector3d to = event.getToTransform().getPosition();
            boolean minor = from.getFloorX() == to.getFloorX() && from.getFloorY() == to.getFloorY() && from.getFloorZ() == to.getFloorZ();
            impact(minecart, cartMechanismBlocks, minor);
        });
    }

    public void impact(Minecart minecart, CartMechanismBlocks blocks, boolean minor) {}

    // TODO if needed. public void enter(Minecart minecart, Player player, CartMechanismBlocks blocks) {}

    public abstract BlockFilter getBlockFilter();

    public boolean requiresSign() {
        return false;
    }

    public boolean isValid(CartMechanismBlocks blocks) {
        return BlockUtil.doesStatePassFilter(getBlockFilter(), blocks.getBase().getBlock()) && (!requiresSign() || blocks.hasSign());
    }

    @Override
    public boolean isValid(Location<World> location) {
        Optional<CartMechanismBlocks> blocks = CartMechanismBlocks.find(location);
        return blocks.filter(this::isValid).isPresent();
    }
}
