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
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

public class TStormSensor extends AbstractSelfTriggeredIC {

    public TStormSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Is It a Storm";
    }

    @Override
    public String getSignTitle() {

        return "IS IT A STORM";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, getSign().getBlock().getWorld().isThundering());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, getSign().getBlock().getWorld().isThundering());
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TStormSensor(getServer(), sign, this);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "High if storming"//Outputs
            };
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if it is storming.";
        }
    }
}