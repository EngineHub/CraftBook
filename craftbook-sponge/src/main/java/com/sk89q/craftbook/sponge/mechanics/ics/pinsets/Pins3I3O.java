/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Pins3I3O extends PinSet {

    @Override
    public int getInputCount() {
        return 3;
    }

    @Override
    public int getOutputCount() {
        return 3;
    }

    @Override
    public String getName() {
        return "3I3O";
    }

    @Override
    public Location<World> getPinLocation(int id, IC ic) {
        switch(id) {
            case 0:
                return ic.getBlock().getRelative(SignUtil.getFront(ic.getBlock()));
            case 1:
                return ic.getBlock().getRelative(SignUtil.getLeft(ic.getBlock()));
            case 2:
                return ic.getBlock().getRelative(SignUtil.getRight(ic.getBlock()));
            case 3:
                return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock()));
            case 4:
                return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getLeft(ic.getBlock()));
            case 5:
                return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getRight(ic.getBlock()));
            default:
                return null;
        }
    }

}
