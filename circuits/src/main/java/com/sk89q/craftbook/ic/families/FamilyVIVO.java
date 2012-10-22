// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractChipState;
import com.sk89q.craftbook.ic.AbstractICFamily;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;

/**
 * Handles detection for the variable-input variable-output family.
 *
 * @author Lymia
 */
public class FamilyVIVO extends AbstractICFamily {

    @Override
    public ChipState detect(BlockWorldVector source, Sign sign) {

        return new ChipStateVIVO(source, sign);
    }

	@Override
	public ChipState detectSelfTriggered(BlockWorldVector source, Sign sign) {

		return new ChipStateVIVO(source, sign, true);
	}


	public static class ChipStateVIVO extends AbstractChipState {

        public ChipStateVIVO(BlockWorldVector source, Sign sign) {

            super(source, sign, false);
        }

	    public ChipStateVIVO(BlockWorldVector source, Sign sign, boolean selfTriggered) {

		    super(source, sign, selfTriggered);
	    }

	    @Override
        protected Block getBlock(int pin) {

            BlockFace fback = SignUtil.getBack(sign.getBlock());
            Block backBlock = sign.getBlock().getRelative(fback);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(sign.getBlock());
                case 1:
                    return SignUtil.getLeftBlock(sign.getBlock());
                case 2:
                    return SignUtil.getRightBlock(sign.getBlock());
                case 3:
                    return backBlock.getRelative(fback);
                case 4:
                    return backBlock.getRelative(SignUtil.getCounterClockWise(fback));
                case 5:
                    return backBlock.getRelative(SignUtil.getClockWise(fback));
                default:
                    return null;

            }

        }

        @Override
        public void set(int pin, boolean value) {

            Block block = getBlock(pin);
            if (isOutput(block)) if (block != null) {
                ICUtil.setState(block, value);
            }
        }

        private boolean isOutput(Block b) {

            return b.getType() == Material.LEVER;
        }

        @Override
        public boolean getInput(int inputIndex) {

            return get(inputIndex);
        }

        @Override
        public boolean getOutput(int outputIndex) {

            return get(outputIndex + 3);
        }

        @Override
        public void setOutput(int outputIndex, boolean value) {

            set(outputIndex + 3, value);
        }

        @Override
        public int getInputCount() {

            return 3;
        }

        @Override
        public int getOutputCount() {

            return 3;
        }

    }

}
