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
package org.enginehub.craftbook.sponge.mechanics.ics.chips.logic;

import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.mechanics.ics.InvalidICException;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.enginehub.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class CombinationLock extends IC {

    public CombinationLock(CombinationLock.Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        if (SignUtil.getTextRaw(lines.get(2)).length() != 3) {
            throw new InvalidICException("The combination must be 3 characters long.");
        }
    }

    @Override
    public void trigger() {
        try {
            char[] data = getLine(2).toCharArray();
            checkCombo:
            {
                if (getPinSet().getInput(0, this) != (data[1] == 'X'))
                    break checkCombo;
                if (getPinSet().getInput(1, this) != (data[2] == 'X'))
                    break checkCombo;
                if (getPinSet().getInput(2, this) != (data[0] == 'X'))
                    break checkCombo;

                getPinSet().setOutput(0, true, this);
                return;
            }
            getPinSet().setOutput(0, false, this);
        } catch (Exception e) {
            getPinSet().setOutput(0, false, this);
        }
    }

    public static class Factory implements ICFactory<CombinationLock> {

        @Override
        public CombinationLock createInstance(Location<World> location) {
            return new CombinationLock(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "The Combination in X's and O's. X being on and O being off.",
                    ""
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "First Input",
                            "Second Input",
                            "Third Input"
                    },
                    new String[] {
                            "High if combination correct"
                    }
            };
        }
    }
}
