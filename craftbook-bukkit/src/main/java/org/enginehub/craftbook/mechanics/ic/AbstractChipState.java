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

package org.enginehub.craftbook.mechanics.ic;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.SignUtil;

/**
 * @author Silthus
 */
public abstract class AbstractChipState implements ChipState {

    protected final ChangedSign sign;
    protected final Location source;
    protected final boolean selfTriggered;
    protected final Block icBlock;

    protected AbstractChipState(Location source, ChangedSign sign, boolean selfTriggered) {

        // Check this here to prevent and handle future NPEs
        Preconditions.checkNotNull(sign, "Null ChangedSign found: " + source.toString());
        this.sign = sign;
        this.source = source;
        this.selfTriggered = selfTriggered;
        icBlock = SignUtil.getBackBlock(sign.getBlock());
    }

    protected abstract Block getBlock(int pin);

    @Override
    public boolean get(int pin) {
        Block block = getBlock(pin);
        if (block == null) return false;
        BlockData data = block.getBlockData();
        if (data instanceof AnaloguePowerable) {
            return ((AnaloguePowerable) data).getPower() > 0;
        }
        if (data instanceof Powerable) {
            return ((Powerable) data).isPowered();
        }
        return block.isBlockIndirectlyPowered();
    }

    @Override
    public void set(int pin, boolean value) {

        Block block = getBlock(pin);
        if (block != null) {
            ICUtil.setState(block, value, icBlock);
        }
    }

    @Override
    public boolean isTriggered(int pin) {
        Block block = getBlock(pin);
        return block != null && BukkitAdapter.adapt(block.getLocation()).equals(source);
    }

    @Override
    public boolean isValid(int pin) {
        Block block = getBlock(pin);
        if (block != null) {
            return block.getType() == Material.REDSTONE_WIRE
                || block.getType() == Material.REPEATER
                || block.getType() == Material.COMPARATOR
                || block.getType() == Material.LEVER;
        }
        return false;
    }
}
