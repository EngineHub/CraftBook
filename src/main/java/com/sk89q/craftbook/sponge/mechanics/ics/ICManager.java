/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics;

import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.*;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.ProgrammableFireworksDisplay;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.WirelessReceiver;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.WirelessTransmitter;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

class ICManager {

    private static SortedSet<ICType<? extends IC>> registeredICTypes = new TreeSet<>(Comparator.comparing(ICType::getModel));

    static {
        //SISO
        registerICType(new ICType<>("MC1000", "REPEATER", "Repeater", "Repeats a redstone signal.", new Repeater.Factory()));
        registerICType(new ICType<>("MC1001", "INVERTER", "Inverter", "Inverts a redstone signal.", new Inverter.Factory()));

        registerICType(new ICType<>("MC1110", "TRANSMITTER", "Wireless Transmitter", "Transmits a wireless redstone signal.", new WirelessTransmitter.Factory()));
        registerICType(new ICType<>("MC1111", "RECEIVER", "Wireless Receiver", "Receives a wireless redstone signal.", new WirelessReceiver.Factory()));

        registerICType(new ICType<>("MC1253", "FIREWORK", "Programmable Firework Display", "Plays a firework show from a file.", new ProgrammableFireworksDisplay.Factory()));

        registerICType(new ICType<>("MC1421", "CLOCK", "Clock", "Outputs high every X ticks when input is high.", new Clock.Factory()));

        //3ISO
        registerICType(new ICType<>("MC3002", "AND", "And Gate", "Outputs high if all inputs are high.", new AndGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3003", "NAND", "Nand Gate", "Outputs high if all inputs are low.", new NandGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3020", "XOR", "Xor Gate", "Outputs high if the inputs are different", new XorGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3021", "XNOR", "Xnor Gate", "Outputs high if the inputs are the same", new XnorGate.Factory(), "3ISO"));
    }

    public static void registerICType(ICType<? extends IC> ic) {
        registeredICTypes.add(ic);
    }

    public static ICType<? extends IC> getICType(String id) {
        for (ICType<? extends IC> icType : registeredICTypes) {
            if (id.equalsIgnoreCase('[' + icType.getModel() + ']')
                    || id.equalsIgnoreCase('=' + icType.getShorthand())
                    || (id.equalsIgnoreCase('[' + icType.getModel() + "]S")
                    || id.equalsIgnoreCase('=' + icType.getShorthand() + " ST")))
                return icType;
        }

        return null;
    }

    public static ICType<? extends IC> getICType(ICFactory<?> icFactory) {
        for (ICType<? extends IC> icType : registeredICTypes) {
            if (icType.getFactory().equals(icFactory)) {
                return icType;
            }
        }

        return null;
    }

    public static Set<ICType<? extends IC>> getICTypes() {
        return registeredICTypes;
    }
}
