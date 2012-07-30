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

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class FlexibleSetBlock extends AbstractIC {

    public FlexibleSetBlock(Server server, Sign sign) {
        super(server, sign);
	    load();
    }

	private void load() {

	}

    @Override
    public String getTitle() {
        return "Flexible Set";
    }

    @Override
    public String getSignTitle() {
        return "FLEX SET";
    }

    @Override
    public void trigger(ChipState chip) {

        String line3 = getSign().getLine(2).toUpperCase();
        String line4 = getSign().getLine(3);

        chip.setOutput(0, chip.getInput(0));

        if (line3.length() < 5) return;

        // Get and validate axis
        String axis = line3.substring(0, 1);
        if (!axis.equals("X") && !axis.equals("Y") && !axis.equals("Z")) return;

        // Get and validate operator
        String op = line3.substring(1, 2);
        if (!op.equals("+") && !op.equals("-")) return;

        // Get and validate distance
        String sdist = line3.substring(2, 3);
        int dist;
        try {
            dist = Integer.parseInt(sdist);
        } catch (Exception e) {
            return;
        }

        if (op.equals("-"))
            dist = -dist;

        // Syntax requires a : at idx 3
        if (!line3.substring(3, 4).equals(":")) return;

        String sblock = line3.substring(4);
        int block;
        try {
            block = Integer.parseInt(sblock);
        } catch (Exception e) {
            return;
        }

        boolean hold = line4.toUpperCase().contains("H");
        boolean inp = chip.getInput(0);

        Block body = SignUtil.getBackBlock(getSign().getBlock());

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (axis.equals("X")) x += dist;
        else if (axis.equals("Y")) y += dist;
        else z += dist;

        if (inp) body.getWorld().getBlockAt(x, y, z).setTypeId(block);
        else if (hold) body.getWorld().getBlockAt(x, y, z).setTypeId(0);

    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new FlexibleSetBlock(getServer(), sign);
        }
    }

}
