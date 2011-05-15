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

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.gates.logic.BothTriggeredIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class DaySensor extends BothTriggeredIC {

    public DaySensor(Server server, Sign sign, boolean selfTriggered, Boolean risingEdge) {
        super(server, sign, selfTriggered, risingEdge, "Day Sensor", "DAY SENSOR");
    }

    /**
     * Returns true if the current time is day.
     * 
     * @return
     */
    private boolean isDay() {
        long time = getSign().getBlock().getWorld().getTime() % 24000;
        if (time < 0)
            time += 24000;
        return (time < 13000l);
    }

    @Override
    public void work(ChipState chip) {
        chip.setOutput(0, isDay());
    }

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new DaySensor(getServer(), sign, false, risingEdge);
        }
    }
    
    public static class FactoryST extends AbstractICFactory {

        public FactoryST(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new DaySensor(getServer(), sign, true, null);
        }
    }

}
