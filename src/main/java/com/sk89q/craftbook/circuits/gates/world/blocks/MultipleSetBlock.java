// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;

public class MultipleSetBlock extends AbstractIC {

    public MultipleSetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    int x, y, z;

    int onblock;
    byte ondata;

    int offblock;
    byte offdata;

    String[] dim;

    @Override
    public void load() {

        String line3 = getSign().getLine(2).replace("+", "").toUpperCase();
        String line4 = getSign().getLine(3);
        getSign().setLine(2, line3);
        getSign().update(false);

        String[] coords = RegexUtil.COLON_PATTERN.split(line3,4);

        Block body = getBackBlock();
        x = body.getX();
        y = body.getY();
        z = body.getZ();

        if (coords.length < 4) return;

        String[] blocks = RegexUtil.MINUS_PATTERN.split(coords[3]);

        String[] onBlocks = RegexUtil.COLON_PATTERN.split(blocks[0],2);
        try {
            onblock = Integer.parseInt(onBlocks[0]);
        } catch (Exception e) {
            return;
        }

        if (onBlocks.length == 2) {
            try {
                ondata = Byte.parseByte(onBlocks[1]);
            } catch (Exception e) {
                return;
            }
        }

        if(blocks.length > 1) {
            String[] offBlocks = RegexUtil.COLON_PATTERN.split(blocks[1],2);

            try {
                offblock = Integer.parseInt(offBlocks[0]);
            } catch (Exception e) {
                offblock = 0;
            }

            if (offBlocks.length == 2) {
                try {
                    offdata = Byte.parseByte(offBlocks[1]);
                } catch (Exception e) {
                    offdata = 0;
                }
            }
        } else {
            offblock = 0;
            offdata = 0;
        }

        x += Integer.parseInt(coords[0]);
        y += Integer.parseInt(coords[1]);
        z += Integer.parseInt(coords[2]);

        dim = RegexUtil.COLON_PATTERN.split(line4);
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

        int setblock = onblock;
        byte setdata = ondata;

        chip.setOutput(0, chip.getInput(0));

        boolean inp = chip.getInput(0);

        if (!inp) {
            setblock = offblock;
            setdata = offdata;
        }

        Block body = getBackBlock();

        if (dim.length == 3) {
            int dimX = Integer.parseInt(dim[0]);
            int dimY = Integer.parseInt(dim[1]);
            int dimZ = Integer.parseInt(dim[2]);
            for (int lx = 0; lx < dimX; lx++) {
                for (int ly = 0; ly < dimY; ly++) {
                    for (int lz = 0; lz < dimZ; lz++) {
                        body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setTypeIdAndData(setblock, setdata, true);
                    }
                }
            }
        } else {
            body.getWorld().getBlockAt(x, y, z).setTypeIdAndData(setblock, setdata, true);
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

        @Override
        public String getShortDescription() {

            return "Sets multiple blocks.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"x:y:z:onid:ondata-offid:offdata", "width:height:depth"};
            return lines;
        }
    }
}