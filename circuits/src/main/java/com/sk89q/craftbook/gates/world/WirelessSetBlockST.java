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
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.SignUtil;

public class WirelessSetBlockST extends AbstractIC implements SelfTriggeredIC {
    
    protected String band;
    
    protected Sign isign;
    
    protected int xOff, zOff;
    protected int blockOn = -1, blockOff = -1;
    protected int dataOn = 0, dataOff = 0;
    
    protected boolean oldLevel = false;

    public WirelessSetBlockST(Server server, Sign sign) {
        super(server, sign);
        
        isign = sign;
        band = sign.getLine(2);
        String line4 = sign.getLine(3);

        //Block b = SignUtil.getBackBlock(getSign().getBlock());
        BlockFace facing = SignUtil.getFacing(sign.getBlock());
        switch (facing) {
        case NORTH:
            xOff = -1;
            break;
        case EAST:
            zOff = -1;
            break;
        case WEST:
            zOff = 1;
            break;
        case SOUTH:
            xOff = 1;
            break;
        }
        
        String strOn = "", strOff = "", strNew;
        
        if (!line4.isEmpty()) {
            if (line4.contains(" ")) {
                strOff = line4.split(" +")[0];
                strOn = line4.split(" +")[1];
            } else {
                strOn = line4;
            }
            if (!strOn.isEmpty()) {
                if (strOn.contains(":")) {
                    try {
                        blockOn = Integer.parseInt(strOn.split(":")[0]);
                        dataOn = Integer.parseInt(strOn.split(":")[1]);
                    } catch (NumberFormatException e) {
                        //meh
                    }
                }
                else {
                    try {
                        blockOn = Integer.parseInt(strOn);
                    } catch (NumberFormatException e) {
                        //meh
                    }
                }
            }
            if (!strOff.isEmpty()) {
                if (strOn.contains(":")) {
                    try {
                        blockOff = Integer.parseInt(strOff.split(":")[0]);
                        dataOff = Integer.parseInt(strOff.split(":")[1]);
                    } catch (NumberFormatException e) {
                        //meh
                    }
                }
                else {
                    try {
                        blockOff = Integer.parseInt(strOff);
                    } catch (NumberFormatException e) {
                        //meh
                    }
                }
            }
        }
        
        boolean found = false;
        
        if (blockOff == -1) {
            int x = isign.getX() + xOff + xOff;
            int y = isign.getY();
            int z = isign.getZ() + zOff + zOff;
            blockOff = isign.getWorld().getBlockAt(x, y, z).getType().getId();
            dataOff = isign.getWorld().getBlockAt(x, y, z).getData();
            found = true;
        }
        
        if (blockOn == -1) {
            int x = isign.getX() + xOff * 3;
            int y = isign.getY();
            int z = isign.getZ() + zOff * 3;
            blockOn = isign.getWorld().getBlockAt(x, y, z).getType().getId();
            dataOn = isign.getWorld().getBlockAt(x, y, z).getData();
            found = true;
        }
        
        if (found && (blockOn != blockOff) || (dataOn != dataOff)) {
            strOn = Integer.toString(blockOn);
            strOff = Integer.toString(blockOff);
            if (dataOn != dataOff) {
                strOn += ":" + Integer.toString(dataOn);
                strOff += ":" + Integer.toString(dataOff);
            }
            sign.setLine(3, strOff + " " + strOn);
        }
        
        //System.out.println("On: " + blockOn + " " + dataOn);
        //System.out.println("Off: " + blockOff + " " + dataOn);
    }

    @Override
    public String getTitle() {
        return "Self-triggered Wireless Set";
    }

    @Override
    public String getSignTitle() {
        return "ST RECV SET";
    }

    @Override
    public void think(ChipState chip) {
            Boolean val = WirelessTransmitter.getValue(band);
            
            if (val == null)
                return;
            
            if (val == oldLevel)
                return;
            
            if ((blockOn == -1) || (blockOff == -1))
                return;
            
            oldLevel = val;
            
            int blockNew = (val ? blockOn : blockOff);
            int dataNew = (val ? dataOn : dataOff);
            
            int x = isign.getX() + xOff + xOff;
            int y = isign.getY();
            int z = isign.getZ() + zOff + zOff;
            
            int blockOld = isign.getWorld().getBlockAt(x, y, z).getType().getId();
            int dataOld = isign.getWorld().getBlockAt(x, y, z).getData();
            
            if (blockNew != blockOld) {
                isign.getWorld().getBlockAt(x, y, z).setTypeId(blockNew);
            }
            if (dataNew != dataOld) {
                isign.getWorld().getBlockAt(x, y, z).setData((byte) dataNew);
            }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new WirelessSetBlockST(getServer(), sign);
        }
    }

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void trigger(ChipState chip) { }

}
