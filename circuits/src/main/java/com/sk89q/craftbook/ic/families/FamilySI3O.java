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

package com.sk89q.craftbook.ic.families;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractChipState;
import com.sk89q.craftbook.ic.AbstractICFamily;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;

/**
 * Handles detection for the single input single output family.
 *
 * @author robhol
 */
public class FamilySI3O extends AbstractICFamily {

    @Override
    public ChipState detect(BlockWorldVector source, Sign sign) {

        return new ChipStateSI3O(source, sign);
    }

	@Override
	public ChipState detectSelfTriggered(BlockWorldVector source, Sign sign) {

		return new ChipStateSI3O(source, sign, true);
	}


	public static class ChipStateSI3O extends AbstractChipState {

        public ChipStateSI3O(BlockWorldVector source, Sign sign) {

            super(source, sign, false);
        }

	    public ChipStateSI3O(BlockWorldVector source, Sign sign, boolean selfTriggered) {

		    super(source, sign, selfTriggered);
	    }

	    @Override
        protected Block getBlock(int pin) {

            Block bsign = sign.getBlock();
            BlockFace fback = SignUtil.getBack(bsign);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(bsign);
                case 1:
                    return bsign.getRelative(fback).getRelative(fback);
                case 2:
                    return bsign.getRelative(fback).getRelative(
                            SignUtil.getCounterClockWise(fback));
                case 3:
                    return bsign.getRelative(fback).getRelative(
                            SignUtil.getClockWise(fback));
                default:
                    return null;
            }

        }

        @Override
        public boolean getInput(int inputIndex) {

            return get(inputIndex);
        }

        @Override
        public boolean getOutput(int outputIndex) {

            return get(outputIndex + 1);
        }

        @Override
        public void setOutput(int outputIndex, boolean value) {

            set(outputIndex + 1, value);
        }

        @Override
        public int getInputCount() {

            return 1;
        }

        @Override
        public int getOutputCount() {

            return 3;
        }

    }

}
