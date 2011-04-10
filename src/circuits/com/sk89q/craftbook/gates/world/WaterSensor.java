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

import static com.sk89q.craftbook.ic.TripleInputChipState.input;
import static com.sk89q.craftbook.ic.TripleInputChipState.output;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;

public class WaterSensor extends AbstractIC {

	protected boolean risingEdge;

	public WaterSensor(Server server, Sign sign, boolean risingEdge) {
		super(server, sign);
		this.risingEdge = risingEdge;
	}

	@Override
	public String getTitle() {
		return "Water Sensor";
	}

	@Override
	public String getSignTitle() {
		return "WATER SENSOR";
	}

	@Override
	public void trigger(ChipState chip) {
		if (risingEdge && input(chip, 0) || (!risingEdge && !input(chip, 0))) {
			output(chip, 0, hasWater());
		}
	}

	/**
	 * Returns true if the sign has water at the specified location.
	 * 
	 * @return
	 */
	private boolean hasWater() {
		
		Block b = SignUtil.getBackBlock(getSign().getBlock());
		
		int x = b.getX();
		int yOffset = b.getY();
		int z = b.getZ();
		try{
            String yOffsetLine = getSign().getLine(2);
            if (yOffsetLine.length() > 0) {
                yOffset += Integer.parseInt(yOffsetLine);
            } else {
                yOffset -= 1;
            }
        } catch (NumberFormatException e) {
            yOffset -= 1;
        }
        int blockID = getSign().getBlock().getWorld().getBlockTypeIdAt(x, yOffset, z);

		return (blockID == 8 || blockID == 9);
	}

	public static class Factory extends AbstractICFactory {

		protected boolean risingEdge;

		public Factory(Server server, boolean risingEdge) {
			super(server);
			this.risingEdge = risingEdge;
		}

		@Override
		public IC create(Sign sign) {
			return new WaterSensor(getServer(), sign, risingEdge);
		}
	}

}
