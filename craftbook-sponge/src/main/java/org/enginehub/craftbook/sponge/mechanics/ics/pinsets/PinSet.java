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
package org.enginehub.craftbook.sponge.mechanics.ics.pinsets;

import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class PinSet {

    public abstract int getInputCount();

    public abstract int getOutputCount();

    public void setOutput(int outputId, boolean powered, IC ic) {

        if(outputId == -1) return;

        if (getOutput(outputId, ic) != powered) {
            Location<?> block = getPinLocation(outputId + getInputCount(), ic);

            if (!block.supports(Keys.POWERED)) return; // Can't set this.

            Sponge.getCauseStackManager().pushCause(ic);
            block.offer(Keys.POWERED, powered);
            Sponge.getCauseStackManager().popCause();
        }
    }

    public int getPinForLocation(IC ic, Location<World> location) {
        for(int i = 0; i < getInputCount() + getOutputCount(); i++)
            if(getPinLocation(i, ic).getBlockPosition().equals(location.getBlockPosition()))
                return i;
        return -1;
    }

    public boolean getInput(int inputId, IC ic) {
        Location<World> pinLocation = getPinLocation(inputId, ic);
        return inputId != -1 && (pinLocation.get(Keys.POWERED).orElse(false)
                || pinLocation.get(Keys.POWER).orElse(0) > 0)
                || pinLocation.getBlockType() == BlockTypes.POWERED_REPEATER
                || pinLocation.getBlockType() == BlockTypes.POWERED_COMPARATOR;
    }

    public boolean getOutput(int outputId, IC ic) {
        return outputId != -1 && getPinLocation(getInputCount() + outputId, ic).get(Keys.POWERED).orElse(false);
    }

    public boolean isValid(int id, IC ic) {
        // TODO Check directions here to make sure it's actually powering the face
        return BlockUtil.isPowerSource(getPinLocation(id, ic));
    }

    public boolean isTriggered(int id, IC ic) {
        return id == ic.getTriggeredPin();
    }

    public abstract String getName();

    public abstract Location<World> getPinLocation(int id, IC ic);
}
