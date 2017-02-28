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
package com.sk89q.craftbook.sponge.mechanics.ics;

import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinSet;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public abstract class IC {

    private ICFactory<? extends IC> icFactory;
    public Location<World> block;
    private Sign sign;
    private PinSet pinSet;

    public IC() {
    }

    public IC(ICFactory<? extends IC> icFactory, Location<World> block) {
        this.icFactory = icFactory;
        this.block = block;
    }

    public PinSet getPinSet() {
        if (pinSet == null) {
            pinSet = ICSocket.PINSETS.get(ICManager.getICType(icFactory).getDefaultPinSet());
        }
        return pinSet;
    }

    /**
     * Called whenever an IC is created for the first time. Setup constant data here.
     *
     * @param player The creating player
     * @param lines The sign lines
     * @throws InvalidICException Thrown if there is an issue with this IC
     */
    public void create(Player player, List<Text> lines) throws InvalidICException {
    }

    /**
     * Called when an IC is loaded into the world.
     */
    public void load() {
    }

    public void unload() {
    }

    public Sign getSign() {
        if (sign == null) {
            sign = (Sign) block.getTileEntity().orElseThrow(() -> new IllegalStateException("IC given block that is not a sign! (" + block.getBlockPosition().toString() + ") in world " + block.getExtent().getName()));
        }

        return sign;
    }

    public String getLine(int line) {
        return SignUtil.getTextRaw(getSign(), line);
    }

    public void setLine(int line, Text text) {
        List<Text> lines = getSign().lines().get();
        lines.set(line, text);
        getSign().offer(Keys.SIGN_LINES, lines);
    }

    public Location<World> getBlock() {
        return this.block;
    }

    public Location<World> getBackBlock() {
        return SignUtil.getBackBlock(this.block);
    }

    public ICFactory<? extends IC> getFactory() {
        return this.icFactory;
    }

    public abstract void trigger();
}
