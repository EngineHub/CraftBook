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
package com.sk89q.craftbook.sponge.mechanics.ics;

import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinSet;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class IC {

    public transient ICType<? extends IC> type;
    public transient Location<World> block;
    public boolean[] pinstates;

    public IC() {}

    public IC(ICType<? extends IC> type, Location<World> block) {
        this.type = type;
        this.block = block;
    }

    public String getPinSetName() {
        return type.getDefaultPinSet();
    }

    public PinSet getPinSet() {
        return ICSocket.PINSETS.get(getPinSetName());
    }

    public void load() {

        PinSet set = getPinSet();
        pinstates = new boolean[set.getInputCount()]; // Just input for now.
    }

    public Location<World> getBlock() {
        return block;
    }

    public ICType<? extends IC> getType() {
        return type;
    }

    public abstract void trigger();

    public boolean[] getPinStates() {
        return pinstates;
    }
}
