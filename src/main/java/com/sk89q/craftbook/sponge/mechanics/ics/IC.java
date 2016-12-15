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
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public abstract class IC {

    /*
     * Due to the way the IC data system works,
     * all non-serializable or non-serialized fields should be transient.
     */

    public transient ICType<? extends IC> type;
    public transient Location<?> block;
    private transient Sign sign;

    private boolean[] pinstates;

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

    /**
     * Called whenever an IC is created for the first time. Setup constant data here.
     *
     * @param player The creating player
     * @param lines The sign lines
     * @throws InvalidICException Thrown if there is an issue with this IC
     */
    public void create(Player player, List<Text> lines) throws InvalidICException {
        PinSet set = getPinSet();
        pinstates = new boolean[set.getInputCount()]; // Just input for now.
    }

    /**
     * Called when an IC is loaded into the world.
     */
    public void load() {
    }

    public Sign getSign() {
        if (sign == null) {
            sign = (Sign) block.getTileEntity().orElseThrow(() -> new IllegalStateException("IC given block that is not a sign!"));
        }

        return sign;
    }

    public String getLine(int line) {
        return SignUtil.getTextRaw(getSign(), line);
    }

    public void setLine(int line, Text text) {
        getSign().lines().set(line, text);
    }

    public Location<?> getBlock() {
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
