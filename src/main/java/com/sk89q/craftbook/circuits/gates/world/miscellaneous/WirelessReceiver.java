// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.ConfigurableIC;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.util.yaml.YAMLProcessor;

public class WirelessReceiver extends AbstractSelfTriggeredIC {

    private String band;

    public WirelessReceiver(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        band = getSign().getLine(2);
        if (!getLine(3).trim().isEmpty())
            band = band + getSign().getLine(3);
    }

    @Override
    public String getTitle() {

        return "Wireless Receiver";
    }

    @Override
    public String getSignTitle() {

        return "RECEIVER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {

            chip.setOutput(0, getOutput());
        }
    }

    public boolean getOutput() {

        Boolean val = WirelessTransmitter.getValue(band);

        if (val == null) {
            return false;
        }

        return val;
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, getOutput());
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        public boolean requirename;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new WirelessReceiver(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Recieves signal from wireless transmitter.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"wireless band", "user"};
        }

        @Override
        public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {

            if (requirename) sign.setLine(3, player.getName());
            else if (!sign.getLine(3).isEmpty()) sign.setLine(3, player.getName());
            sign.update(false);
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            requirename = config.getBoolean(path + "per-player", false);
        }
    }

    @Override
    public boolean isActive () {
        return true;
    }
}