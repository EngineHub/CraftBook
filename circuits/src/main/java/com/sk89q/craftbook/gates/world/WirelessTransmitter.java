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
import com.sk89q.craftbook.util.HistoryHashMap;

public class WirelessTransmitter extends AbstractIC {

    protected static final HistoryHashMap<String, Boolean> memory
    = new HistoryHashMap<String, Boolean>(100);

    protected String band;

    public WirelessTransmitter(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
        try {
            band = sign.getLine(2);
        }
        catch(Exception e){
            band = "test";
        }
    }

    @Override
    public String getTitle() {

        return "Wireless Transmitter";
    }

    @Override
    public String getSignTitle() {

        return "TRANSMITTER";
    }

    @Override
    public void trigger(ChipState chip) {

        setValue(band, chip.getInput(0));
        chip.setOutput(0, chip.getInput(0));
    }

    public static Boolean getValue(String band) {

        return memory.get(band);
    }

    public static void setValue(String band, boolean val) {

        memory.put(band, val);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new WirelessTransmitter(getServer(), sign, this);
        }
    }
}