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

import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A helper class that stores information about the
 * blocks used by a {@link SpongeCartBlockMechanic}.
 */
public class CartMechanismBlocks {

    private Location<World> rail;
    private Location<World> base;
    private Location<World> sign;

    private CartMechanismBlocks(Location<World> rail, Location<World> base, Location<World> sign) {
        this.rail = rail;
        this.base = base;
        this.sign = sign;
    }

    /**
     * Checks if this {@link CartMechanismBlocks} contains a sign.
     *
     * @return If it contains a sign
     */
    public boolean hasSign() {
        return sign != null && SignUtil.isSign(sign);
    }

    public Location<World> getRail() {
        return this.rail;
    }

    public Location<World> getBase() {
        return this.base;
    }

    public Location<World> getSign() {
        return this.sign;
    }

    /**
     * Check if the given {@link BlockState} matches this.
     *
     * @param blockState The given blockstate
     * @return If it matches
     */
    public boolean matches(BlockState blockState) {
        return base.getBlock().equals(blockState);
    }

    /**
     * Check if the given text matches the sign.
     *
     * @param text The given text
     * @return If it matches
     */
    public boolean matches(String text) {
        return hasSign() && SignUtil.getTextRaw(sign.getTileEntity()
                .map(sign -> (Sign) sign).get(), 1).equalsIgnoreCase('[' + text + ']');
    }

    /**
     * Gets the {@link CartMechanismBlocks} as a list.
     *
     * <p>
     *     The list will contain the following order,
     *     rail, base, sign (if present)
     * </p>
     *
     * @return This object as a list
     */
    public List<Location<World>> asList() {
        List<Location<World>> list = new ArrayList<>();
        list.add(rail);
        list.add(base);
        if (hasSign()) {
            list.add(sign);
        }
        return list;
    }

    /**
     * Find a {@link CartMechanismBlocks} from an unknown starting block.
     *
     * @param block The block to search from.
     * @return The CartMechanismBlocks, if found
     */
    public static Optional<CartMechanismBlocks> find(Location<World> block) {
        if (SignUtil.isSign(block)) {
            return findBySign(block);
        } else if (BlockUtil.isTrack(block.getBlockType())) {
            return findByRail(block);
        } else {
            return findByBase(block);
        }
    }

    /**
     * Find a {@link CartMechanismBlocks} from a sign.
     *
     * @param block The sign to search from.
     * @return The CartMechanismBlocks, if found
     */
    public static Optional<CartMechanismBlocks> findBySign(Location<World> block) {
        if (!SignUtil.isSign(block)) {
            return Optional.empty();
        }
        if (BlockUtil.isTrack(block.getRelative(Direction.UP).getRelative(Direction.UP).getBlockType())) {
            return Optional.of(new CartMechanismBlocks(
                    block.getRelative(Direction.UP).getRelative(Direction.UP),
                    block.getRelative(Direction.UP),
                    block));
        } else if (BlockUtil.isTrack(block.getRelative(Direction.UP).getRelative(Direction.UP).getRelative(Direction.UP).getBlockType())) {
            return Optional.of(new CartMechanismBlocks(
                    block.getRelative(Direction.UP).getRelative(Direction.UP).getRelative(Direction.UP),
                    block.getRelative(Direction.UP).getRelative(Direction.UP),
                    block));
        } else if  (BlockUtil.isTrack(block.getRelative(SignUtil.getBack(block)).getRelative(Direction.UP).getBlockType())) {
            return Optional.of(new CartMechanismBlocks(
                    block.getRelative(SignUtil.getBack(block)).getRelative(Direction.UP),
                    block.getRelative(SignUtil.getBack(block)),
                    block));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Find a {@link CartMechanismBlocks} from a rail.
     *
     * @param block The rail to search from.
     * @return The CartMechanismBlocks, if found
     */
    public static Optional<CartMechanismBlocks> findByRail(Location<World> block) {
        if (!BlockUtil.isTrack(block.getBlockType())) {
            return Optional.empty();
        }
        Location<World> base = block.getRelative(Direction.DOWN);
        if (SignUtil.isSign(base.getRelative(Direction.DOWN))) {
            return Optional.of(new CartMechanismBlocks(
                    block,
                    base,
                    base.getRelative(Direction.DOWN)));
        } else if (SignUtil.isSign(base.getRelative(Direction.DOWN).getRelative(Direction.DOWN))) {
            return Optional.of(new CartMechanismBlocks(
                    block,
                    base,
                    base.getRelative(Direction.DOWN).getRelative(Direction.DOWN)));
        } else {
            Location<World> sign = null;
            for (Direction direction : BlockUtil.getDirectHorizontalFaces()) {
                if (SignUtil.isSign(base.getRelative(direction))) {
                    sign = base.getRelative(direction);
                    break;
                }
            }

            return Optional.of(new CartMechanismBlocks(block, base, sign));
        }
    }

    /**
     * Find a {@link CartMechanismBlocks} from the base.
     *
     * <p>
     *    This delegates to {@link CartMechanismBlocks#findByRail}.
     * </p>
     *
     * @param block The base block to search from
     * @return The CartMechanismBlocks, if found
     */
    public static Optional<CartMechanismBlocks> findByBase(Location<World> block) {
        Location<World> rail = block.getRelative(Direction.UP);
        return findByRail(rail);
    }
}
