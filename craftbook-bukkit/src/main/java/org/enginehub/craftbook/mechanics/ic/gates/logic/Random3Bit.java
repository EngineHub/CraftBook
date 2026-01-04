/*
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

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

public class Random3Bit extends RandomBit {

    public Random3Bit(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Random 3-Bit";
    }

    @Override
    public String getSignTitle() {

        return "3-BIT RANDOM";
    }

    public static class Factory extends RandomBit.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC2020''' generates 3 random bits whenever the input (the \"clock\") goes from low to high."
            };
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new Random3Bit(getServer(), sign, this);
        }
    }

}
