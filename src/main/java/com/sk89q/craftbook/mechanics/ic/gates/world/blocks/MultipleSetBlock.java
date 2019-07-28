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

package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import java.util.Locale;

import com.sk89q.craftbook.util.BlockSyntax;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import org.bukkit.block.data.BlockData;

public class MultipleSetBlock extends AbstractIC {

    public MultipleSetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    private int x, y, z;

    private BlockData onBlock;
    private BlockData offblock;

    private String[] dim;

    @Override
    public void load() {

        String line3 = getSign().getLine(2).replace("+", "").toUpperCase(Locale.ENGLISH);
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

        onBlock = BlockSyntax.getBukkitBlock(blocks[0]);

        offblock = (blocks.length > 1) ? BlockSyntax.getBukkitBlock(blocks[1]) : Material.AIR.createBlockData();

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

        BlockData setBlock = onBlock;

        chip.setOutput(0, chip.getInput(0));

        boolean inp = chip.getInput(0);

        if (!inp) {
            setBlock = offblock;
        }

        Block body = getBackBlock();

        if (dim.length == 3) {
            int dimX = Integer.parseInt(dim[0]);
            int dimY = Integer.parseInt(dim[1]);
            int dimZ = Integer.parseInt(dim[2]);
            for (int lx = 0; lx < dimX; lx++) {
                for (int ly = 0; ly < dimY; ly++) {
                    for (int lz = 0; lz < dimZ; lz++) {
                        body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setBlockData(setBlock, true);
                    }
                }
            }
        } else {
            body.getWorld().getBlockAt(x, y, z).setBlockData(setBlock, true);
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

            return new String[] {"x:y:z:onid:ondata-offid:offdata", "width:height:depth"};
        }
    }
}