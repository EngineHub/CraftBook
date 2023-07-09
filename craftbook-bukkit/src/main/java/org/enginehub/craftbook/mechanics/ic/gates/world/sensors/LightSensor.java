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

package org.enginehub.craftbook.mechanics.ic.gates.world.sensors;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.SignUtil;

public class LightSensor extends AbstractSelfTriggeredIC {

    public LightSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Light Sensor";
    }

    @Override
    public String getSignTitle() {

        return "LIGHT SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, hasLight());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hasLight());
    }

    @Override
    public void load() {

        if (!getLine(3).isEmpty())
            centre = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        else
            centre = SignUtil.getBackBlock(getSign().getBlock()).getLocation().add(0, 1, 0);

        try {
            min = Byte.parseByte(getLine(2));
        } catch (Exception e) {
            min = 10;
            try {
                getSign().setLine(2, Component.text(min));
                getSign().update(false);
            } catch (Exception ignored) {
            }
        }
    }

    Location centre;
    byte min;

    /**
     * Returns true if the sign has a light level above the specified.
     *
     * @return
     */
    private boolean hasLight() {

        byte lightLevel = centre.getBlock().getLightLevel();

        return lightLevel >= min;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LightSensor(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if specific block is above specified light level.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "minimum light", "x:y:z offset" };
        }
    }
}
