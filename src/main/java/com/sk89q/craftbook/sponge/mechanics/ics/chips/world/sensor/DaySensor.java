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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.sensor;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.SelfTriggeringIC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class DaySensor extends IC implements SelfTriggeringIC {

    public DaySensor(ICFactory<DaySensor> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    private long day;
    private long night;

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        try {
            if (!SignUtil.getTextRaw(lines.get(2)).isEmpty()) {
                Long.parseLong(SignUtil.getTextRaw(lines.get(2)));
            }
        } catch (NumberFormatException e) {
            throw new InvalidICException("Start time entered incorrectly");
        }
        try {
            if (!SignUtil.getTextRaw(lines.get(3)).isEmpty()) {
                Long.parseLong(SignUtil.getTextRaw(lines.get(3)));
            }
        } catch (NumberFormatException e) {
            throw new InvalidICException("End time entered incorrectly");
        }
    }

    @Override
    public void load() {
        super.load();

        try {
            night = Long.parseLong(getLine(3));
        } catch (Exception ignored) {
            night = 13000L;
        }
        try {
            day = Long.parseLong(getLine(2));
        } catch (Exception ignored) {
            day = 0L;
        }
    }

    @Override
    public void think() {
        getPinSet().setOutput(0, isDay(), this);
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            getPinSet().setOutput(0, isDay(), this);
        }
    }

    private boolean isDay() {
        long time = getBlock().getExtent().getProperties().getWorldTime() % 24000;

        if (day < night) {
            return time >= day && time <= night;
        } else if (day > night) {
            return time <= day || time >= night;
        }
        return time < night;
    }

    public static class Factory implements ICFactory<DaySensor> {

        @Override
        public DaySensor createInstance(Location<World> location) {
            return new DaySensor(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "Start Time",
                    "End Time"
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Clock"
                    },
                    new String[] {
                            "If day (Or within time bounds)"
                    }
            };
        }
    }
}