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

import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.AndGate;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Clock;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.CombinationLock;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Dispatcher;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.EdgeTriggerDFlipFlop;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Inverter;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.LevelTriggerDFlipFlop;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.MemoryAccess;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.MemorySetter;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.NandGate;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.RSNandLatch;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.RSNorLatch;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.RandomBit;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Repeater;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.ToggleFlipFlop;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.WorldTimeModulus;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.XnorGate;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.XorGate;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.block.BlockReplacer;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.ProgrammableFireworksDisplay;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.WirelessReceiver;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.WirelessTransmitter;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.ZeusBolt;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.world.sensor.DaySensor;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import org.spongepowered.api.Sponge;

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

        registerICType(new ICType<>("MC1017", "RE T FLIP", "Toggle Flip Flop RE", "Toggles output on high.", new ToggleFlipFlop.Factory(true)));
        registerICType(new ICType<>("MC1018", "FE T FLIP", "Toggle Flip Flip FE", "Toggles output on low.", new ToggleFlipFlop.Factory(false)));

        registerICType(new ICType<>("MC1020", "RANDOM BIT", "Random Bit", "Randomly sets the output on high.", new RandomBit.Factory()));

        registerICType(new ICType<>("MC1025", "TIME MODULUS", "World Time Modulus", "Outputs high when the world time is odd.", new WorldTimeModulus.Factory()));

        registerICType(new ICType<>("MC1110", "TRANSMITTER", "Wireless Transmitter", "Transmits a wireless redstone signal.", new WirelessTransmitter.Factory()));
        registerICType(new ICType<>("MC1111", "RECEIVER", "Wireless Receiver", "Receives a wireless redstone signal.", new WirelessReceiver.Factory()));

        registerICType(new ICType<>("MC1203", "ZEUS BOLT", "Zeus Bolt", "Strikes a location with lightning.", new ZeusBolt.Factory()));

        registerICType(new ICType<>("MC1230", "SENSE DAY", "Daylight Sensor", "Outputs high if it is day.", new DaySensor.Factory()));

        registerICType(new ICType<>("MC1249", "BLOCK REPLACER", "Block Replacer", "Searches a nearby area and replaces blocks accordingly.", new BlockReplacer.Factory()));

        registerICType(new ICType<>("MC1253", "FIREWORK", "Programmable Firework Display", "Plays a firework show from a file.", new ProgrammableFireworksDisplay.Factory()));

        registerICType(new ICType<>("MC1421", "CLOCK", "Clock", "Outputs high every X ticks when input is high.", new Clock.Factory()));

        //SI3O
        registerICType(new ICType<>("MC2300", "ROM GET", "ROM Get", "Gets the memory state from a file for usage in the MemorySetter/Access IC group.", new MemoryAccess.Factory(), "SI3O"));

        //3ISO
        registerICType(new ICType<>("MC3002", "AND", "And Gate", "Outputs high if all inputs are high.", new AndGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3003", "NAND", "Nand Gate", "Outputs high if all inputs are low.", new NandGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3020", "XOR", "Xor Gate", "Outputs high if the inputs are different", new XorGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3021", "XNOR", "Xnor Gate", "Outputs high if the inputs are the same", new XnorGate.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3030", "RS-NOR", "RS-Nor Latch", "A compact RS-Nor Latch", new RSNorLatch.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3033", "RS-NAND", "RS-Nand Latch", "A compact RS-Nand Latch", new RSNandLatch.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3034", "EDGE-D", "Edge-Trigger D Flip Flop", "A compact Edge-D Flip Flop", new EdgeTriggerDFlipFlop.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3036", "LEVEL-D", "Level-Trigger D Flip Flop", "A compact Level-D Flip Flop", new LevelTriggerDFlipFlop.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3050", "COMBO", "Combination Lock", "Outputs high if the correct combination is inputed", new CombinationLock.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3300", "ROM SET", "ROM Set", "Sets the memory state for a file for usage in the MemorySetter/Access IC group.", new MemorySetter.Factory(), "3ISO"));

        //3I3O
        registerICType(new ICType<>("MC4200", "DISPATCH", "Dispatcher", "Outputs the centre input on the appropriate outputs when input is high.", new Dispatcher.Factory(), "3I3O"));
    }

    public static void registerICType(ICType<? extends IC> ic) {
        registeredICTypes.add(ic);

        if (ic.getFactory() instanceof SerializedICFactory) {
            Sponge.getDataManager().registerBuilder(((SerializedICFactory) ic.getFactory()).getRequiredClass(), ((SerializedICFactory) ic.getFactory()));
        }
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
