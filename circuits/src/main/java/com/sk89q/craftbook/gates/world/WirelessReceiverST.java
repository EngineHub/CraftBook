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
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class WirelessReceiverST extends AbstractIC implements SelfTriggeredIC {

    protected final String band;

    protected final Sign iSign;

    public WirelessReceiverST(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);

        iSign = sign;
        if(sign != null)
            band = sign.getLine(2);
        else
            band = "";
    }

    @Override
    public String getTitle() {

        return "Self-triggered Wireless Receiver";
    }

    @Override
    public String getSignTitle() {

        return "ST RECEIVER";
    }

    @Override
    public void think(ChipState chip) {

        Boolean val = WirelessTransmitter.getValue(band);

        if (val == null) return;

        chip.setOutput(0, val);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new WirelessReceiverST(getServer(), sign, this);
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

    }

}
