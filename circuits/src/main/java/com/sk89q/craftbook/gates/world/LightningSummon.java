// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;



public class LightningSummon extends AbstractIC {

    public LightningSummon(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Zeus Bolt";
    }

    @Override
    public String getSignTitle() {
        return "ZEUS BOLT";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            Location loc = SignUtil.getBackBlock(getSign().getBlock()).getLocation();
            if (getSign().getLine(2).length() != 0) {
                try {
                    double y = Integer.parseInt(getSign().getLine(2)) + loc.getY();
                    loc.setY(y);
                }
                catch (NumberFormatException e) {
                    return;
                }
            }
            getSign().getWorld().strikeLightning(loc);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new LightningSummon(getServer(), sign);
        }
    }
}


