// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.gates.world;

import static com.sk89q.craftbook.ic.TripleInputChipState.input;
import static com.sk89q.craftbook.ic.TripleInputChipState.output;
import org.bukkit.Server;
import org.bukkit.block.Block;
import com.sk89q.craftbook.gates.logic.FallingToggleFlipFlop;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class RisingServerTimeModulus extends AbstractIC {

    public RisingServerTimeModulus(Server server, Block block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Rising Server Time Modulus";
    }

    @Override
    public void trigger(ChipState chip) {
        if (input(chip, 0)) {
            output(chip, 0, isServerTimeOdd());
        }
    }

    /**
     * Returns true if the relative time is odd.
     * 
     * @return
     */
    private boolean isServerTimeOdd() {
        long time = getBlock().getWorld().getTime() % 2;
        if (time < 0) time += 2;
        return (time == 1);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Block block) {
            return new FallingToggleFlipFlop(getServer(), block);
        }
    }

}
