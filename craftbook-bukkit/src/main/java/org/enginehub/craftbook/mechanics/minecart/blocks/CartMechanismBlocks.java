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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.BoundingBox;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.minecart.RailUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.jspecify.annotations.Nullable;

/**
 * <p>
 * Stores the tuple of three blocks over which any typical CartMechanism is implemented,
 * and also performs detection of two of the blocks if only one
 * is given.
 * </p>
 * <p>
 * Sign text and base block type are not validated by any constructors; this must be performed
 * explicitly by calling
 * the appropriate methods after
 * construction. Selection of signs thus does not concern itself with sign text at all; but this is
 * fine,
 * since if you have two signs in an area where
 * they could concievably contend for control of the mechanism, you would be doing something that
 * ought be physically
 * impossible anyway (though yes,
 * it is possible if editing the world directly without physics).
 * </p>
 */
public record CartMechanismBlocks(Block rail, Block base, Block sign) {

    /**
     * Determines if the given sign includes any of the given text.
     *
     * @param testText The text on the sign to test against.
     * @return a Side if the bracketed keyword on the sign matches the given testText; null otherwise
     *     or if no sign.
     */
    public @Nullable Side matches(String... testText) {
        if (!hasSign()) {
            return null;
        }
        Sign bukkitSign = (Sign) sign.getState(false);
        for (Side side : Side.values()) {
            for (String test : testText) {
                if (bukkitSign.getSide(side).getLine(1).equalsIgnoreCase("[" + test + "]")) {
                    return side;
                }
            }
        }
        return null;
    }

    public boolean matches(String testText, Side side) {
        if (!hasSign()) {
            return false;
        }
        return PlainTextComponentSerializer.plainText().serialize(getChangedSign(side).getLine(1)).equalsIgnoreCase("[" + testText + "]");
    }

    /**
     * Gets whether this CartMechanismBlocks instance matches the given block.
     *
     * @param mat The {@link BaseBlock} material.
     * @return if the base block is the same type as the given block.
     */
    public boolean matches(BaseBlock mat) {
        return mat.equalsFuzzy(BukkitAdapter.adapt(base.getBlockData()));
    }

    public boolean hasSign() {
        return sign != null && SignUtil.isSign(sign);
    }

    public boolean hasRail() {
        return rail != null;
    }

    public boolean hasBase() {
        return base != null;
    }

    public ChangedSign getChangedSign(Side side) {
        return hasSign() ? ChangedSign.create(sign, side) : null;
    }

    /**
     * Attempts to find a Minecart on the rails.
     *
     * @return a Minecart if one is found within the given block, or null if none found. (If there
     *     is more than one
     *     minecart within the block, the
     *     first one encountered when traversing the list of Entity in the Chunk is the one
     *     returned.)
     */
    public @Nullable Minecart findMinecart() {
        if (!hasRail()) {
            return null;
        }

        BoundingBox railBox = BoundingBox.of(rail);
        for (Entity ent : rail.getChunk().getEntities()) {
            if (!(ent instanceof Minecart cart)) {
                continue;
            }
            if (railBox.contains(cart.getBoundingBox())) {
                return cart;
            }
        }

        return null;
    }

    /**
     * Detecting factory; defers to one of the other three specific detecting factories based on
     * whether the given
     * unknown block appears to be a sign,
     * rail, or base.
     *
     * @param unknown the block to examine.
     */
    public static @Nullable CartMechanismBlocks find(Block unknown) {
        Material ti = unknown.getType();

        if (SignUtil.isSign(unknown)) {
            return findBySign(unknown);
        } else if (RailUtil.isTrack(ti)) {
            return findByRail(unknown);
        } else {
            return findByBase(unknown);
        }
    }

    /**
     * <p>
     * Detecting factory, based on the position of the rails. The base must be one block below and
     * the sign if it
     * exists must be two or three blocks
     * below. Signs are guaranteed to be signs (unless they're null) and rails are guaranteed to be
     * rails.
     * </p>
     * <p>
     * This is the most important constructor, since it is the one invoked when processing cart move
     * events.
     * </p>
     *
     * @param rail the block containing the rails.
     */
    public static @Nullable CartMechanismBlocks findByRail(Block rail) {
        if (!RailUtil.isTrack(rail.getType())) {
            return null;
        }

        BlockFace face = BlockFace.DOWN;

        if (rail.getType() == Material.LADDER) {
            face = ((Directional) rail.getBlockData()).getFacing().getOppositeFace();
        } else if (rail.getType() == Material.VINE) {
            MultipleFacing vine = (MultipleFacing) rail.getBlockData();
            for (BlockFace test : vine.getAllowedFaces()) {
                if (vine.hasFace(test)) {
                    face = test.getOppositeFace();
                    break;
                }
            }
        }

        if (SignUtil.isSign(rail.getRelative(face, 2))) {
            return new CartMechanismBlocks(rail, rail.getRelative(face, 1), rail.getRelative(face, 2));
        } else if (SignUtil.isSign(rail.getRelative(face, 3))) {
            return new CartMechanismBlocks(rail, rail.getRelative(face, 1), rail.getRelative(face, 3));
        } else if (SignUtil.isSign(rail.getRelative(face, 1).getRelative(BlockFace.EAST, 1))) {
            return new CartMechanismBlocks(rail, rail.getRelative(face, 1), rail.getRelative(face, 1).getRelative(BlockFace.EAST, 1));
        } else if (SignUtil.isSign(rail.getRelative(face, 1).getRelative(BlockFace.WEST, 1))) {
            return new CartMechanismBlocks(rail, rail.getRelative(face, 1), rail.getRelative(face, 1).getRelative(BlockFace.WEST, 1));
        } else if (SignUtil.isSign(rail.getRelative(face, 1).getRelative(BlockFace.NORTH, 1))) {
            return new CartMechanismBlocks(rail, rail.getRelative(face, 1), rail.getRelative(face, 1).getRelative(BlockFace.NORTH, 1));
        } else if (SignUtil.isSign(rail.getRelative(face, 1).getRelative(BlockFace.SOUTH, 1))) {
            return new CartMechanismBlocks(rail, rail.getRelative(face, 1), rail.getRelative(face, 1).getRelative(BlockFace.SOUTH, 1));
        }

        return new CartMechanismBlocks(rail, rail.getRelative(face, 1), null);
    }

    /**
     * Detecting factory, based on the position of the base. The rails must be one block above and
     * the sign if it
     * exists must be one or two blocks
     * below. Signs are guaranteed to be signs (unless they're null) and rails are guaranteed to be
     * rails.
     *
     * @param base the block on which the rails sit; the type of this block is what determines
     *     the mechanism type.
     */
    private static @Nullable CartMechanismBlocks findByBase(Block base) {
        if (!RailUtil.isTrack(base.getRelative(BlockFace.UP, 1).getType())) {
            return null;
        }

        if (SignUtil.isSign(base.getRelative(BlockFace.DOWN, 1))) {
            return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, base.getRelative(BlockFace.DOWN, 1));
        } else if (SignUtil.isSign(base.getRelative(BlockFace.DOWN, 2))) {
            return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, base.getRelative(BlockFace.DOWN, 2));
        } else if (SignUtil.isSign(base.getRelative(BlockFace.EAST, 1))) {
            return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, base.getRelative(BlockFace.EAST, 1));
        } else if (SignUtil.isSign(base.getRelative(BlockFace.WEST, 1))) {
            return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, base.getRelative(BlockFace.WEST, 1));
        } else if (SignUtil.isSign(base.getRelative(BlockFace.NORTH, 1))) {
            return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, base.getRelative(BlockFace.NORTH, 1));
        } else if (SignUtil.isSign(base.getRelative(BlockFace.SOUTH, 1))) {
            return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, base.getRelative(BlockFace.SOUTH, 1));
        }

        return new CartMechanismBlocks(base.getRelative(BlockFace.UP, 1), base, null);
    }

    /**
     * Detecting factory, based on the position of the sign. The base must be one or two blocks
     * above and the rails
     * an additional block above the
     * base. Signs are guaranteed to be signs and rails are guaranteed to be rails.
     *
     * @param sign the block containing the sign that gives additional configuration to the
     *     mechanism.
     */
    private static @Nullable CartMechanismBlocks findBySign(Block sign) {
        if (!SignUtil.isSign(sign)) {
            return null;
        }

        if (RailUtil.isTrack(sign.getRelative(BlockFace.UP, 2).getType())) {
            return new CartMechanismBlocks(sign.getRelative(BlockFace.UP, 2), sign.getRelative(BlockFace.UP, 1), sign);
        } else if (RailUtil.isTrack(sign.getRelative(BlockFace.UP, 3).getType())) {
            return new CartMechanismBlocks(sign.getRelative(BlockFace.UP, 3), sign.getRelative(BlockFace.UP, 2), sign);
        } else if (RailUtil.isTrack(sign.getRelative(SignUtil.getBack(sign), 1).getRelative(BlockFace.UP, 1).getType())) {
            return new CartMechanismBlocks(sign.getRelative(SignUtil.getBack(sign), 1).getRelative(BlockFace.UP, 1), sign.getRelative(SignUtil.getBack(sign), 1), sign);
        }

        return null;
    }
}
