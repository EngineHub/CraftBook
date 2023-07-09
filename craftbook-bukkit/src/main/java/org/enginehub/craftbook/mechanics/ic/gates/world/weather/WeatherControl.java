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
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.RegexUtil;

public class WeatherControl extends AbstractIC {

    public WeatherControl(Server server, ChangedSign sign, ICFactory factory) {

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

        boolean tstorm = false;
        int duration = 24000;
        int thunderDuration = duration;
        try {
            String[] st = RegexUtil.RIGHT_BRACKET_PATTERN.split(getLine(1), 2);
            if (st.length > 1) {
                tstorm = st[1].equalsIgnoreCase("t");
            }
            duration = Integer.parseInt(getLine(2));
        } catch (Exception ignored) {
        }
        try {
            thunderDuration = Integer.parseInt(getLine(3));
        } catch (Exception ignored) {
        }

        if (duration > 24000) {
            duration = 24000;
        }
        if (duration < 1) {
            duration = 1;
        }

        if (thunderDuration > 24000) {
            thunderDuration = 24000;
        }
        if (thunderDuration < 1) {
            thunderDuration = 1;
        }

        World world = getSign().getBlock().getWorld();
        if (chip.getInput(0)) {
            world.setStorm(true);
            world.setWeatherDuration(duration);
            if (tstorm) {
                world.setThundering(true);
                world.setThunderDuration(thunderDuration);
            }
            chip.setOutput(0, true);
        } else {
            world.setThundering(false);
            world.setStorm(false);
            chip.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new WeatherControl(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Set rain and thunder duration.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Set weather state",//Inputs
                "Input 1"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "rain duration", "thunder duration" };
        }
    }
}