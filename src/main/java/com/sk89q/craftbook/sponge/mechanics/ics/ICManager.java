/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

class ICManager {

    private static SortedSet<ICType<? extends IC>> registeredICTypes = new TreeSet<>((o1, o2) -> o1.modelId.compareTo(o2.modelId));

    static {
        //SISO
        registerICType(new ICType<>("MC1000", "REPEATER", "Repeater", "Repeats a redstone signal.", Repeater.class));
        registerICType(new ICType<>("MC1001", "INVERTER", "Inverter", "Inverts a redstone signal.", Inverter.class));

        registerICType(new ICType<>("MC1421", "CLOCK", "Clock", "Outputs high every X ticks when input is high.", Clock.class));

        //3ISO
        registerICType(new ICType<>("MC3002", "AND", "And Gate", "Outputs high if all inputs are high.", AndGate.class, "3ISO"));
        registerICType(new ICType<>("MC3003", "NAND", "Nand Gate", "Outputs high if all inputs are low.", NandGate.class, "3ISO"));
        registerICType(new ICType<>("MC3020", "XOR", "Xor Gate", "Outputs high if the inputs are different", XorGate.class, "3ISO"));
        registerICType(new ICType<>("MC3021", "XNOR", "Xnor Gate", "Outputs high if the inputs are the same", XnorGate.class, "3ISO"));
    }

    public static void registerICType(ICType<? extends IC> ic) {
        registeredICTypes.add(ic);
    }

    public static ICType<? extends IC> getICType(String id) {
        for (ICType<? extends IC> icType : registeredICTypes) {
            if (id.equalsIgnoreCase('[' + icType.modelId + ']')
                    || id.equalsIgnoreCase('=' + icType.shorthandId)
                    || id.equalsIgnoreCase('[' + icType.modelId + "]S")
                    || id.equalsIgnoreCase('=' + icType.shorthandId + " ST"))
                return icType;
        }

        return null;
    }

    public static Set<ICType<? extends IC>> getICTypes() {
        return registeredICTypes;
    }
}
