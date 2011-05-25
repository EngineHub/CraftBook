// $Id$
/*
 * Copyright (C) 2011 purpleposeidon@gmail.com
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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;


/*
 * [MC1290] Sign Spinner
 * 2nd line: How many steps to spin. This is a value from -16 to +16.
 * Every 4 is a full block rotation. Blocks will rotate only if the rotation is a multiple of 4.
 * 
 * The following blocks rotate: Signs, pumpkins, diodes, furnaces, dispensers, stairs, rails
 * The following blocks transfer rotational power: fence posts, soul sand
 */

public class SignSpinner extends AbstractIC {

    protected boolean risingEdge;

    public SignSpinner(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Rotor";
    }

    @Override
    public String getSignTitle() {
        return "ROTOR";
    }

	@Override
	public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            Location loc = SignUtil.getBackBlock(getSign().getBlock()).getLocation();
            int rotation;
            try {
            	rotation = Integer.parseInt(getSign().getLine(2));
            }
            catch (NumberFormatException e) {
            	rotation = 1;
            }
            rotation %= 16;
            byte blockRotation = 0;
            if ((rotation % 4) == 0)
            	blockRotation = (byte) (rotation / 4);
            
            for (int i = 0; i < 16; i++) {
            	loc.setY(loc.getY()+1);
            	Block here = loc.getBlock();
            	Material m = here.getType();
            	int data = here.getData();
            	if (m == Material.FENCE) {} //spins the fence posts
            	else if (m == Material.SOUL_SAND) {} //spins in grave
            	else if (m == Material.SIGN_POST) {
            		data = data + rotation % 16;
            	}
            	else if (m == Material.PUMPKIN || m == Material.JACK_O_LANTERN) {
            		data = (data + blockRotation) % 4;
            	}
            	//and now it all goes to hell
            	//(WorldEdit's BlockDataCycler does not rotate things properly)
            	else if (m == Material.DIODE_BLOCK_ON || m == Material.DIODE_BLOCK_OFF) {
            		int direction = data & 0x3;
            		direction = (direction+blockRotation) % 4;
            		data &= ~0x3;
            		data |= direction;
            	}
            	else if (m == Material.FURNACE || m == Material.BURNING_FURNACE || m == Material.DISPENSER) {
            		data -= 2;
            		//EWNS
            		byte rotationsMap[][] = {
            				{0, 3, 1, 2},
            				{1, 2, 0, 3},
            				{2, 0, 3, 1},
            				{3, 1, 2, 0}
            		};
            		data = rotationsMap[data][blockRotation];
            		data += 2;
            	}
            	else if (m == Material.WOOD_STAIRS || m == Material.COBBLESTONE_STAIRS) {
            		//SNWE
            		byte rotationsMap[][] = {
            				{0, 2, 1, 3},
            				{1, 3, 0, 2},
            				{2, 1, 3, 0},
            				{3, 0, 2, 1}
            		};
            		data = rotationsMap[data][blockRotation];
            	}
            	else if (m == Material.DETECTOR_RAIL || m == Material.RAILS || m == Material.POWERED_RAIL) {
            		byte powerState = 0;
            		if (m == Material.POWERED_RAIL) {
            			//Doesn't have rotations, instead has power
            			powerState = (byte) (data & 0x8);
            			data ^= powerState;
            		}
            		if (data == 0 || data == 1) {
            			//east-west or north-south rails
	            		if (blockRotation % 2 == 1) {
	            			if (data == 0) data = 1;
	            			else data = 0;
	            		}
	            		//otherwise, it'd be back where it was
            		}
            		else if (2 <= data && data <= 5) {
            			//A ramp
            			data -= 2;
                		//SNWE
                		byte rotationsMap[][] = {
                				{0, 2, 1, 3},
                				{1, 3, 0, 2},
                				{2, 1, 3, 0},
                				{3, 0, 2, 1}
                		};
                		data = rotationsMap[data][blockRotation];
            			data += 2;
            		}
            		else if (6 <= data && data <= 9) {
            			//curved rail
            			data -= 6;
            			data = (data + blockRotation) % 4;
            			data += 6;
            			//should be a regular rail!
            		}
            		data |= powerState; //put the power state back in
            	}
            	else { //stop at an unspinable block
            		break;
            	}
            	here.setData((byte) data, true);
            }
        }
	}
	
    
    public static class Factory extends AbstractICFactory implements RestrictedIC {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new SignSpinner(getServer(), sign, risingEdge);
        }
    }
}


