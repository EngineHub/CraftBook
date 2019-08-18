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
package org.enginehub.craftbook.sponge.mechanics.ics;

import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.AndGate;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.Clock;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.CombinationLock;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.Counter;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.Dispatcher;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.DownCounter;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.EdgeTriggerDFlipFlop;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.FullAdder;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.FullSubtractor;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.HalfAdder;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.HalfSubtractor;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.InvertedRSNandLatch;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.Inverter;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.JKFlipFlop;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.LevelTriggerDFlipFlop;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.MemoryAccess;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.MemorySetter;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.NandGate;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.RSNandLatch;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.RSNorLatch;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.RandomBit;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.Repeater;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.ToggleFlipFlop;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.WorldTimeModulus;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.XnorGate;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.logic.XorGate;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.block.BlockReplacer;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.entity.EntitySpawner;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.entity.ItemDispenser;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.ProgrammableFireworksDisplay;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.WirelessReceiver;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.WirelessTransmitter;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.miscellaneous.ZeusBolt;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.sensor.DaySensor;
import org.enginehub.craftbook.sponge.mechanics.ics.chips.world.weather.TimeControlAdvanced;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import org.enginehub.craftbook.sponge.mechanics.ics.plc.PlcFactory;
import org.enginehub.craftbook.sponge.mechanics.ics.plc.lang.Perlstone;
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

        registerICType(new ICType<>("MC1200", "SPAWNER", "Entity Spawner", "Spawns an entity with specified data.", new EntitySpawner.Factory()));
        registerICType(new ICType<>("MC1201", "DISPENSER", "Item Dispenser", "Spawns in items with specified data.", new ItemDispenser.Factory()));

        registerICType(new ICType<>("MC1203", "ZEUS BOLT", "Zeus Bolt", "Strikes a location with lightning.", new ZeusBolt.Factory()));

        registerICType(new ICType<>("MC1230", "SENSE DAY", "Daylight Sensor", "Outputs high if it is day.", new DaySensor.Factory()));

        registerICType(new ICType<>("MC1249", "BLOCK REPLACER", "Block Replacer", "Searches a nearby area and replaces blocks accordingly.", new BlockReplacer.Factory()));

        registerICType(new ICType<>("MC1253", "FIREWORK", "Programmable Firework Display", "Plays a firework show from a file.", new ProgrammableFireworksDisplay.Factory()));

        registerICType(new ICType<>("MC1421", "CLOCK", "Clock", "Outputs high every X ticks when input is high.", new Clock.Factory()));

        //SI3O
        registerICType(new ICType<>("MC2020", "RANDOM 3", "Random 3-Bit", "Randomly sets the outputs on high.", new RandomBit.Factory(), "SI3O"));

        registerICType(new ICType<>("MC2300", "ROM GET", "ROM Get", "Gets the memory state from a file for usage in the MemorySetter/Access IC group.", new MemoryAccess.Factory(), "SI3O"));

        //3ISO
        registerICType(new ICType<>("MC3002", "AND", "And Gate", "Outputs high if all inputs are high.", new AndGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3003", "NAND", "Nand Gate", "Outputs high if all inputs are low.", new NandGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3020", "XOR", "Xor Gate", "Outputs high if the inputs are different", new XorGate.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3021", "XNOR", "Xnor Gate", "Outputs high if the inputs are the same", new XnorGate.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3030", "RS-NOR", "RS-Nor Latch", "A compact RS-Nor Latch", new RSNorLatch.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3031", "INV RS-NAND", "Inverse RS-Nand Latch", "A compact Inverse RS-Nand Latch", new InvertedRSNandLatch.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3032", "JK FLIP", "JK Flip Flip", "A compact JK Flip Flop", new JKFlipFlop.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3033", "RS-NAND", "RS-Nand Latch", "A compact RS-Nand Latch", new RSNandLatch.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3034", "EDGE-D", "Edge-Trigger D Flip Flop", "A compact Edge-D Flip Flop", new EdgeTriggerDFlipFlop.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3036", "LEVEL-D", "Level-Trigger D Flip Flop", "A compact Level-D Flip Flop", new LevelTriggerDFlipFlop.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3050", "COMBO", "Combination Lock", "Outputs high if the correct combination is inputed", new CombinationLock.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3101", "DOWN COUNTER", "Down Counter", "Decrements on redstone signal, outputs high when reset.", new DownCounter.Factory(), "3ISO"));
        registerICType(new ICType<>("MC3102", "COUNTER", "Counter", "Increments on redstone signal, outputs high when reset.", new Counter.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3231", "T CONTROL ADV", "Time Control Advanced",
                "Changes the time of day when the clock input goes from low to high.", new TimeControlAdvanced.Factory(), "3ISO"));

        registerICType(new ICType<>("MC3300", "ROM SET", "ROM Set", "Sets the memory state for a file for usage in the MemorySetter/Access IC group.", new MemorySetter.Factory(), "3ISO"));

        //3I3O
        registerICType(new ICType<>("MC4000", "FULL ADDER", "Full Adder", "A compact full-adder", new FullAdder.Factory(), "3I3O"));
        registerICType(new ICType<>("MC4010", "HALF ADDER", "Half Adder", "A compact half-adder", new HalfAdder.Factory(), "3I3O"));

        registerICType(new ICType<>("MC4100", "FULL SUBTR", "Full Subtractor", "A compact full-subtractor", new FullSubtractor.Factory(), "3I3O"));
        registerICType(new ICType<>("MC4110", "HALF SUBTR", "Half Subtractor", "A compact half-subtractor", new HalfSubtractor.Factory(), "3I3O"));
        registerICType(new ICType<>("MC4200", "DISPATCH", "Dispatcher", "Outputs the centre input on the appropriate outputs when input is high.", new Dispatcher.Factory(), "3I3O"));

        //PLC
        registerICType(new ICType<>("MC5000", "PERLSTONE", "Perlstone 3ISO Programmable Logic Chip", "3ISO PLC programmable with Perlstone.", new PlcFactory<>(new Perlstone()), "3ISO"));

        registerICType(new ICType<>("MC6020", "RANDOM 5", "Random 5-Bit", "Randomly sets the outputs on high.", new RandomBit.Factory(), "SI5O"));
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
