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
package org.enginehub.craftbook.sponge.mechanics.ics.chips.world.weather;

import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.mechanics.ics.RestrictedIC;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class TimeControlAdvanced  extends IC {

    public TimeControlAdvanced(ICFactory<TimeControlAdvanced> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void trigger() {
        if (getPinSet().isTriggered(0, this) && getPinSet().getInput(0, this)) {
            int partialDayTime = (int) (getBlock().getExtent().getProperties().getWorldTime() % 24000);
            int time = (int) (getBlock().getExtent().getProperties().getWorldTime() - partialDayTime);
            int timeOffset; // Start off setting to the next day.

            // Generate a time offset. This means that it'll always set the next time occurance.
            if (getPinSet().getInput(1, this)) {
                timeOffset = partialDayTime <= 1000 ? 1000 : (1000 + 24000);
            } else {
                timeOffset = partialDayTime <= 13000 ? 13000 : (13000 + 24000);
            }

            getBlock().getExtent().getProperties().setWorldTime(time + timeOffset);
        }
    }

    public static class Factory implements ICFactory<TimeControlAdvanced>, RestrictedIC {

        @Override
        public TimeControlAdvanced createInstance(Location<World> location) {
            return new TimeControlAdvanced(this, location);
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Set Time",
                            "Day on High, Night on Low",
                            "Nothing"
                    },
                    new String[] {
                            "Nothing"
                    }
            };
        }
    }
}
