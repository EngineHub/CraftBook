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

package org.enginehub.craftbook.mechanics.ic.gates.world.weather;

import org.bukkit.Server;
import org.bukkit.World;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;

public class WeatherControlAdvanced extends AbstractIC {

    public WeatherControlAdvanced(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Weather Control";
    }

    @Override
    public String getSignTitle() {

        return "WEATHER CONTROL";
    }

    @Override
    public void trigger(ChipState chip) {

        int duration = 24000;
        int thunderDuration = duration;

        try {
            duration = Integer.parseInt(getLine(2));
            thunderDuration = Integer.parseInt(getLine(3));
        } catch (Exception ignored) {
        }

        if (duration > 24000) {
            duration = 24000;
        } else if (duration < 1) {
            duration = 1;
        }

        if (thunderDuration > 24000) {
            thunderDuration = 24000;
        } else if (thunderDuration < 1) {
            thunderDuration = 1;
        }

        if (chip.isTriggered(0) && chip.getInput(0)) {

            World world = getSign().getBlock().getWorld();
            world.setStorm(chip.getInput(1));
            if (chip.getInput(1)) {
                world.setWeatherDuration(duration);
            }
            world.setThundering(chip.getInput(2));
            if (chip.getInput(2)) {
                world.setThunderDuration(thunderDuration);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new WeatherControlAdvanced(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "When centre on, set rain if left high and thunder if right high.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "High to rain",
                "High to thunder",
                "High on success"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "rain duration", "thunder duration" };
        }
    }
}