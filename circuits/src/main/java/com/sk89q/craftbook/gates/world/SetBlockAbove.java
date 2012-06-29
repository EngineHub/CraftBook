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
import com.sk89q.worldedit.blocks.BlockType;


public class SetBlockAbove extends AbstractIC {

    public SetBlockAbove(Server server, Sign sign) {
	super(server, sign);
    }

    @Override
    public String getTitle() {
	return "Set Block Above";
    }

    @Override
    public String getSignTitle() {
	return "SET BLOCK ABOVE";
    }

    @Override
    public void trigger(ChipState chip) {

	String sblockdat = getSign().getLine(2).toUpperCase().trim();
	String sblock = sblockdat.split(":")[0];
	String smeta = "";
	if(sblockdat.split(":").length>1)
	    smeta = sblockdat.split(":")[1];
	String force = getSign().getLine(3).toUpperCase().trim();

	chip.setOutput(0, chip.getInput(0));

	int block = -1;
	BlockType bt = BlockType.lookup(sblock, true);
	if(bt != null) block = bt.getID();

	//FIXME hack for broken WorldEdit <=5.1
	if(block == -1)
	    try {
		block = Integer.parseInt(sblock);
	    } catch (Exception e) {
		return;
	    }

	byte meta = -1;
	try {
	    if(!smeta.equalsIgnoreCase(""))
		meta = Byte.parseByte(smeta);
	} catch (Exception e) {
	    return;
	}


	Block body = SignUtil.getBackBlock(getSign().getBlock());

	int x = body.getX();
	int y = body.getY();
	int z = body.getZ();

	if(force.equals("FORCE") || body.getWorld().getBlockAt(x, y+1, z).getType() == Material.AIR) {
	    body.getWorld().getBlockAt(x, y+1, z).setTypeId(block);
	    if(!(meta==-1))
		body.getWorld().getBlockAt(x, y+1, z).setData(meta);
	}
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

	public Factory(Server server) {
	    super(server);
	}

	@Override
	public IC create(Sign sign) {
	    return new SetBlockAbove(getServer(), sign);
	}
    }

}
