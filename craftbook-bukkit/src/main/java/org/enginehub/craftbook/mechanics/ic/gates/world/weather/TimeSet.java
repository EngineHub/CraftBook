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

package com.sk89q.craftbook.mechanics.ic.gates.world.weather;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;

public class TimeSet extends AbstractSelfTriggeredIC {

    public TimeSet(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Time Set";
    }

    @Override
    public String getSignTitle() {

        return "TIME SET";
    }

    @Override
    public void load() {

        try {
            time = Long.parseLong(getSign().getLine(2));
        } catch (NumberFormatException ex) {
            time = -1;
        }
    }

    /* it's been a */ long time;

    @Override
    public void trigger(ChipState chip) {

        try {
            if (chip.getInput(0) && time >= 0) {
                CraftBookBukkitUtil.toSign(getSign()).getWorld().setTime(time);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void think(ChipState chip) {

        try {
            if (chip.getInput(0) && time >= 0) {
                CraftBookBukkitUtil.toSign(getSign()).getWorld().setTime(time);
            }
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimeSet(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Set time when triggered.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"time to set", null};
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }
}