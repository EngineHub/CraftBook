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
package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class PinSet {

    public abstract int getInputCount();

    public abstract int getOutputCount();

    public void setInput(int inputId, boolean powered, IC ic) {
        if(inputId == -1) return;
        ic.getPinStates()[inputId] = powered;
    }

    public void setOutput(int outputId, boolean powered, IC ic) {

        if(outputId == -1) return;

        if (getOutput(outputId, ic) != powered) {
            Location<?> block = getPinLocation(outputId + getInputCount(), ic);

            if (!block.supports(Keys.POWERED)) return; // Can't set this.

            block.offer(Keys.POWERED, powered);
        }
    }

    public int getPinForLocation(IC ic, Location<World> location) {
        for(int i = 0; i < getInputCount() + getOutputCount(); i++)
            if(getPinLocation(i, ic).getBlockPosition().equals(location.getBlockPosition()))
                return i;
        return -1;
    }

    public boolean getInput(int inputId, IC ic) {
        return inputId != -1 && ic.getPinStates()[inputId];
    }

    public boolean getOutput(int outputId, IC ic) {
        return outputId != -1 && getPinLocation(getInputCount() + outputId, ic).get(Keys.POWERED).orElse(false);
    }

    public boolean isValid(int id, IC ic) {
        BlockType type = getPinLocation(id, ic).getBlockType();

        return type == BlockTypes.REDSTONE_WIRE || type == BlockTypes.LEVER;
    }

    public abstract String getName();

    public abstract Location<?> getPinLocation(int id, IC ic);
}
