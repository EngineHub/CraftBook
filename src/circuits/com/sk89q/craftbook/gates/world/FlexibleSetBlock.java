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

public class FlexibleSetBlock extends AbstractIC {

    public FlexibleSetBlock(Server server, Sign sign) {
        super(server, sign);
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
        String line4 = getSign().getLine(3).toUpperCase();

        chip.setOutput(0, chip.getInput(0));

        if (line3.length() < 5)
            return;

        // Get and validate axis
        String axis = line3.substring(0, 1);
        if (!axis.equals("X") && !axis.equals("Y") && !axis.equals("Z"))
            return;

        // Get and validate operator
        String op = line3.substring(1, 2);
        if (!op.equals("+") && !op.equals("-"))
            return;

        // Get and validate distance
        String sdist = line3.substring(2, 3);
        int dist = -1;
        try {
            dist = Integer.parseInt(sdist);
        } catch (Exception e) {
            return;
        }

        if (op.equals("-"))
            dist = -dist;

        // Syntax requires a : at idx 3
        if (!line3.substring(3, 4).equals(":"))
            return;

<<<<<<< HEAD
        // The remaining string must be either a type id or a typeid:datavalue.
        // No type id may contain a colon, so this will not break any existing blocks.
        String[] onblockparams = line3.substring(4).split(":");
        int onblocktype = 0;
        Byte onblockdata = 0;
        boolean setonblockdata = false;
        if (onblockparams.length > 0) {
	        try {
	        	onblocktype = Integer.parseInt(onblockparams[0]);
	        } catch (Exception e) { return; }
        }
        if (onblockparams.length > 1) {
	        try {
	        	onblockdata = Byte.parseByte(onblockparams[1]);
	        	setonblockdata = true;
	        } catch (Exception e) { setonblockdata = false; }
        }
        
        // Syntax for line 4 (optional): [<any string containing an h>|<typeid>[:<dataval>]]
        // 
        // Strings containing an h are equivalent to the number 0. On a falling edge, the target
        // block will be replaced with the specified block type and data value, if specified.
        // 
        // No existing line 4 string will parse properly according to this syntax, and will therefore
        // be ignored.
        String[] offblockparams = line4.split(":");
        int offblocktype = 0;
        boolean setoffblocktype = false;
        Byte offblockdata = 0;
        boolean setoffblockdata = false;
        if (line4.contains("H")) {
        	offblocktype = 0;
        	setoffblocktype = true;
        } else {
            if (offblockparams.length > 0) {
	            try {
	            	offblocktype = Integer.parseInt(offblockparams[0]);
	            	setoffblocktype = true;
	            } catch (Exception e) { setoffblocktype = false; }
            }
            if (offblockparams.length > 1) {
            	try {
	            	offblockdata = Byte.parseByte(offblockparams[1]);
	            	setoffblockdata = true;
	            } catch (Exception e) { setoffblockdata = false; }
            }
=======
        String sonblock = line3.substring(4);
        int onblock = -1;
        try {
            onblock = Integer.parseInt(sonblock);
        } catch (Exception e) {
            return;
>>>>>>> 99674799c617b2f7fcba2721a340f5969e9ba303
        }
        
        // Syntax for line 4 (optional): H[:<off block type>]
        // Causes the block to be toggled between two types
        int offblock = 0;
        if (line4.length() > 2) {
            String soffblock = line4.substring(2);
            try {
                offblock = Integer.parseInt(soffblock);
            } catch (Exception e) {
                return;
            }
        }

<<<<<<< HEAD
=======
        boolean hold = line4.contains("H");
>>>>>>> 99674799c617b2f7fcba2721a340f5969e9ba303
        boolean inp = chip.getInput(0);

        Block body = SignUtil.getBackBlock(getSign().getBlock());

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (axis.equals("X"))
            x += dist;
        else if (axis.equals("Y"))
            y += dist;
        else
            z += dist;

<<<<<<< HEAD
        if (inp) {
        	Block targetblock = body.getWorld().getBlockAt(x, y, z);
        	targetblock.setTypeId(onblocktype);
        	if (setonblockdata) targetblock.setData(onblockdata);
        } else if (setoffblocktype) {
        	Block targetblock = body.getWorld().getBlockAt(x, y, z);
        	targetblock.setTypeId(offblocktype);
        	if (setoffblockdata) targetblock.setData(offblockdata);
        }
=======
        if (inp)
            body.getWorld().getBlockAt(x, y, z).setTypeId(onblock);
        else if (hold)
            body.getWorld().getBlockAt(x, y, z).setTypeId(offblock);
>>>>>>> 99674799c617b2f7fcba2721a340f5969e9ba303

    }

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new FlexibleSetBlock(getServer(), sign);
        }
    }

}
