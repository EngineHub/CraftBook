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

import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.SelfTriggeredIC;
import org.enginehub.craftbook.util.ICUtil;

/**
 * @author Silthus
 */
public class ItemNotSensor extends ItemSensor implements SelfTriggeredIC {

    public ItemNotSensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Item Not Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ITEM NOT SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, !isDetected());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, !isDetected());
    }

    public static class Factory extends ItemSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemNotSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getShortDescription() {

            return "Detects if an item is NOT within a given radius";
        }
    }
}