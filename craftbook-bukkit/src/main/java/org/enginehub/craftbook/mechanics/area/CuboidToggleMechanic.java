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

package org.enginehub.craftbook.mechanics.area;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.exception.InvalidMechanismException;
import org.enginehub.craftbook.util.BlockUtil;
import org.jspecify.annotations.Nullable;

/**
 * A class that can be a mechanic that toggles a cuboid. This is basically either Door or Bridge.
 */
public abstract class CuboidToggleMechanic extends StoredBlockMechanic {

    public CuboidToggleMechanic(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    /**
     * Gets the sign on the other side of this mechanic.
     *
     * @param nearSign The near sign to search from
     * @return The far sign, if it exists
     */
    public abstract @Nullable Block getFarSign(Block nearSign);

    public abstract CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidMechanismException;

    public boolean open(Sign sign, BlockData blockData, CuboidRegion toggle) {
        for (BlockVector3 bv : toggle) {
            Block checkBlock = sign.getWorld().getBlockAt(bv.x(), bv.y(), bv.z());
            BlockData checkType = checkBlock.getBlockData();
            if (checkType.getMaterial() == blockData.getMaterial() || BlockUtil.isBlockReplacable(checkType.getMaterial())) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction && (checkType.getMaterial() == blockData.getMaterial())) {
                    addToStoredBlockCount(sign, getCostOfBlock(checkType));
                }
                checkBlock.setType(Material.AIR);
            }
        }

        return true;
    }

    public boolean close(Sign sign, Sign farSide, BlockData blockData, CuboidRegion toggle) {
        for (BlockVector3 bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.x(), bv.y(), bv.z());
            if (BlockUtil.isBlockReplacable(b.getType())) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction) {
                    if (getStoredBlockCounts(sign, farSide) > 0) {
                        b.setBlockData(blockData);
                        takeFromStoredBlockCounts(getCostOfBlock(blockData), sign, farSide);
                    } else {
                        return false;
                    }
                } else {
                    b.setBlockData(blockData);
                }
            }
        }

        return true;
    }
}
