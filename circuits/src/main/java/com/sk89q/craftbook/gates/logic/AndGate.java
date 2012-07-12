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

package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class AndGate extends AbstractIC {

    public AndGate(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "And Gate";
    }

    @Override
    public String getSignTitle() {
        return "AND";
    }
    @Override
    public void trigger(ChipState chip) {

        int on = 0, valid = 0;
        for (int i = 0; i < chip.getInputCount(); i++) {

            if (chip.isValid(i))
            {
                valid++;

                if (chip.getInput(i))
                    on++;
            }
        }

        // Condition; all valid must be ON, at least one valid.
        chip.setOutput(0, (on == valid && valid > 0));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new AndGate(getServer(), sign);
        }
    }

}
