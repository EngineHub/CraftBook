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

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class TimeControl extends AbstractIC {

    public TimeControl(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Time Control";
    }

    @Override
    public String getSignTitle() {

        return "TIME CONTROL";
    }

    @Override
    public void trigger(ChipState chip) {

        Long time;
        if (chip.getInput(0))
            time = 0L;
        else
            time = 13000L;
        getSign().getWorld().setTime(time);

        chip.setOutput(0, chip.getInput(0));
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new TimeControl(getServer(), sign);
        }
    }
}
