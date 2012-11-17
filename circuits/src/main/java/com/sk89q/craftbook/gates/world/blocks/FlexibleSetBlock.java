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

package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;

public class FlexibleSetBlock extends AbstractIC {

    public FlexibleSetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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

        // Valid Line 3:
        // [axis][sign][distance]:[blockTypeId]:[blockData]
        // axis is one of X, Y, Z
        // sign is optional or one of "+" or "-"
        // blockData is optional (along with its preceding colon
        String line3 = getSign().getLine(2).toUpperCase();

        String line4 = getSign().getLine(3);

        chip.setOutput(0, chip.getInput(0));

        String[] params = ICUtil.COLON_PATTERN.split(line3);
        if (params.length < 2) return;
        if (params[0].length() < 2) return;

        // Get and validate axis
        String axis = params[0].substring(0, 1);
        if (!axis.equals("X") && !axis.equals("Y") && !axis.equals("Z")) return;

        // Get and validate operator (default +)
        String op = params[0].substring(1, 2);
        int distStart = 2;
        if (!op.equals("+") && !op.equals("-")) {
            op = "+";
            distStart = 1; // no op so distance starts after axis
        }

        // Get and validate distance
        String sdist = params[0].substring(distStart);
        int dist;
        try {
            dist = Integer.parseInt(sdist);
        } catch (Exception e) {
            return;
        }

        if (op.equals("-")) {
            dist = -dist;
        }

        int block;
        try {
            block = Integer.parseInt(params[1]);
        } catch (Exception e) {
            return;
        }

        // default block data is 0
        byte data = 0;
        if (params.length > 2) {
            try {
                data = Byte.parseByte(params[2]);
            } catch (Exception e) {
                return;
            }
        }

        boolean hold = line4.toUpperCase().contains("H");
        boolean inp = chip.getInput(0);

        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (axis.equals("X")) {
            x += dist;
        } else if (axis.equals("Y")) {
            y += dist;
        } else {
            z += dist;
        }

        if (inp) {
            body.getWorld().getBlockAt(x, y, z).setTypeIdAndData(block, data, true);
        } else if (hold) {
            body.getWorld().getBlockAt(x, y, z).setTypeId(0);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FlexibleSetBlock(getServer(), sign, this);
        }
    }

}
