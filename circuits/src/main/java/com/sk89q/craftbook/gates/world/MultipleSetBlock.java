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
        coords = line3.split(":");
        
        if ( coords.length != 4 )
        	return;

        int block = -1;
        try {
            block = Integer.parseInt(coords[3]);
        } catch (Exception e) {
            return;
        }

        if ( !coords[0].substring(0, 1).equals("0") ) {
	        if ( coords[0].substring(0, 1).equals("+") ) {
	        	x += Integer.parseInt(coords[0].substring(1));
	        } else {
	        	x -= Integer.parseInt(coords[0].substring(1));
	        }
        }

        if ( !coords[1].substring(0, 1).equals("0") ) {
	        if ( coords[1].substring(0, 1).equals("+") ) {
	        	y += Integer.parseInt(coords[1].substring(1));
	        } else {
	        	y -= Integer.parseInt(coords[1].substring(1));
	        }
        }

        if ( !coords[2].substring(0, 1).equals("0") ) {
	        if ( coords[2].substring(0, 1).equals("+") ) {
	        	z += Integer.parseInt(coords[2].substring(1));
	        } else {
	        	z -= Integer.parseInt(coords[2].substring(1));
	        }
        }

        if (inp) {
        	if ( dim.length == 3 ) {
	        	for( int lx = -1; lx < (Integer.parseInt(dim[0]) - 1); lx++ ) {
	        		for( int ly = -1; ly < (Integer.parseInt(dim[1]) - 1); ly++ ) {
	        			for( int lz = -1; lz < (Integer.parseInt(dim[2]) - 1); lz++ ) {
	        				body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setTypeId(block);
	        			}
	        		}
	        	}
        	} else {
        		body.getWorld().getBlockAt(x, y, z).setTypeId(block);
        	}
        } else {
        	if ( dim.length == 3 ) {
	        	for( int lx = -1; lx < (Integer.parseInt(dim[0]) - 1); lx++ ) {
	        		for( int ly = -1; ly < (Integer.parseInt(dim[1]) - 1); ly++ ) {
	        			for( int lz = -1; lz < (Integer.parseInt(dim[2]) - 1); lz++ ) {
	        				body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setTypeId(0);
	        			}
	        		}
	        	}
        	} else {
        		body.getWorld().getBlockAt(x, y, z).setTypeId(0);
        	}
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
