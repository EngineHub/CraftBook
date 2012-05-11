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

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class XnorGate extends AbstractIC {

    public XnorGate(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "XNOR Gate";
    }

    @Override
    public String getSignTitle() {

        return "XNOR";
    }

    @Override
    public void trigger(ChipState chip) {

        Boolean a = null;
        Boolean b = null;

        //New input handling: any/first two valid inputs discovered. Moar flexibility!
        for (int i = 0; i < chip.getInputCount(); i++) {
            if (chip.isValid(i)) {
                boolean pinval = chip.getInput(i);
                //Got pin value, assign to first free variable, break if got both.
                if (a == null) a = pinval;
                else if (b == null) b = pinval;
                else break;
            }
        }

        if (a == null || b == null)
            return;


        boolean result = (a && b) || (!a && !b);
        chip.setOutput(0, result);

    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new XnorGate(getServer(), sign);
        }
    }

}
