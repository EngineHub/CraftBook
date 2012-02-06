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
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;

public class TimeControlAdvanced extends AbstractIC {

    protected boolean risingEdge;

    public TimeControlAdvanced(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Advanced Time Control";
    }

    @Override
    public String getSignTitle() {
        return "ADV TIME CONTROL";
    }

	@Override
	public void trigger(ChipState chip) {
		if (chip.isTriggered(0) && chip.getInput(0)) {
			int time;
			boolean t = chip.get(1);
			if (t)
				time = (8 - 8 + 24) * 1000;
			else
				time = (0 - 8 + 24) * 1000;
			getSign().getWorld().setTime(time);
		}
	}

    public static class Factory extends AbstractICFactory implements RestrictedIC {


        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new TimeControlAdvanced(getServer(), sign);
        }
    }
}
