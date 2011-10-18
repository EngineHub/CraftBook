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

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class MultipleSetBlock extends AbstractIC {

    public MultipleSetBlock(Server server, Sign sign) {
        super(server, sign);
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

        String[] dim = line4.split(":");

        Block body = SignUtil.getBackBlock(getSign().getBlock());
        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        String[] coords;
        coords = line3.replaceAll("\\+","").split(":");
        
        if ( coords.length != 4 )
            return;

        int block = -1;
        try {
            block = Integer.parseInt(coords[3]);
        } catch (Exception e) {
            return;
        }

        x += Integer.parseInt(coords[0]);
        y += Integer.parseInt(coords[1]);
        z += Integer.parseInt(coords[2]);

        if (!inp) {
            block = 0;
        }

        if ( dim.length == 3 ) {
            for( int lx = 0; lx < (Integer.parseInt(dim[0])); lx++ ) {
                for( int ly = 0; ly < (Integer.parseInt(dim[1])); ly++ ) {
                    for( int lz = 0; lz < (Integer.parseInt(dim[2])); lz++ ) {
                        body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setTypeId(block);
                    }
                }
            }
        } else {
            body.getWorld().getBlockAt(x, y, z).setTypeId(block);
        }
    }

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new MultipleSetBlock(getServer(), sign);
        }
    }

}
