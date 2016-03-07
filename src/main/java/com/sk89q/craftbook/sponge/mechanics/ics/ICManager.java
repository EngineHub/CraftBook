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

import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.AndGate;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Clock;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Inverter;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Repeater;

import java.util.HashSet;
import java.util.Set;

public class ICManager {

    public static Set<ICType<? extends IC>> registeredICTypes = new HashSet<>();

    static {
        registerICType(new ICType<>("MC1000", "REPEATER", Repeater.class));
        registerICType(new ICType<>("MC1001", "INVERTER", Inverter.class));

        registerICType(new ICType<>("MC1421", "CLOCK", Clock.class));

        registerICType(new ICType<>("MC3002", "AND", AndGate.class, "3ISO"));
    }

    public static void registerICType(ICType<? extends IC> ic) {

        registeredICTypes.add(ic);
    }

    public static ICType<? extends IC> getICType(String id) {

        for (ICType<? extends IC> icType : registeredICTypes) {
            if (id.equalsIgnoreCase("[" + icType.modelId + "]")
                    || id.equalsIgnoreCase("=" + icType.shorthandId)
                    || id.equalsIgnoreCase("[" + icType.modelId + "]S")
                    || id.equalsIgnoreCase("=" + icType.shorthandId + " ST"))
                return icType;
        }

        return null;
    }
}
