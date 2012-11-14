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

import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class MultipleSetBlock extends AbstractIC {

    private static final Pattern PLUS_PATTERN = Pattern.compile("+", Pattern.LITERAL);

    public MultipleSetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Multiple SetBlock";
    }

    @Override
    public String getSignTitle() {

        return "MULTI-SET BLOCK";
    }

    @Override
    public void trigger(ChipState chip) {

        String line3 = getSign().getLine(2).toUpperCase();
        String line4 = getSign().getLine(3);

        chip.setOutput(0, chip.getInput(0));

        boolean inp = chip.getInput(0);

        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        String[] coords;
        coords = ICUtil.COLON_PATTERN.split(PLUS_PATTERN.matcher(line3).replaceAll(""));

        if (coords.length < 4)
            return;

        int block;
        try {
            block = Integer.parseInt(coords[3]);
        } catch (Exception e) {
            return;
        }

        byte data = 0;
        if (coords.length == 5) {
            try {
                data = Byte.parseByte(coords[4]);
            } catch (Exception e) {
                return;
            }
        }

        x += Integer.parseInt(coords[0]);
        y += Integer.parseInt(coords[1]);
        z += Integer.parseInt(coords[2]);

        if (!inp) {
            block = 0;
        }

        String[] dim = ICUtil.COLON_PATTERN.split(line4);
        if (dim.length == 3) {
            int dimX = Integer.parseInt(dim[0]);
            int dimY = Integer.parseInt(dim[1]);
            int dimZ = Integer.parseInt(dim[2]);
            for (int lx = 0; lx < dimX; lx++) {
                for (int ly = 0; ly < dimY; ly++) {
                    for (int lz = 0; lz < dimZ; lz++) {
                        body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setTypeIdAndData(block, data, true);
                    }
                }
            }
        } else {
            body.getWorld().getBlockAt(x, y, z).setTypeIdAndData(block, data, true);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MultipleSetBlock(getServer(), sign, this);
        }
    }

}
