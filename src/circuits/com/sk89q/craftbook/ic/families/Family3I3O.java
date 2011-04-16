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

import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractICFamily;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

/**
 * Handles detection for the triple-input triple-output family.
 * 
 * @author robhol
 */
public class Family3I3O extends AbstractICFamily {

    @Override
    public ChipState detect(BlockWorldVector source, Sign sign) {
        return new ChipState3I3O(source, sign);
    }

    public static class ChipState3I3O implements ChipState {

        protected Sign sign;
        protected BlockWorldVector source;

        public ChipState3I3O(BlockWorldVector source, Sign sign) {
            this.sign = sign;
            this.source = source;
        }

        protected Block getBlock(int pin) {

            // TODO: messes up based on direction.

            Block bsign = sign.getBlock();
            BlockFace fback = SignUtil.getBack(bsign);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(bsign);
                case 1:
                    return SignUtil.getLeftBlock(sign.getBlock());
                case 2:
                    return SignUtil.getRightBlock(sign.getBlock());
                case 3:
                    return bsign.getRelative(fback).getRelative(fback)
                            .getRelative(fback);
                case 4:
                    return bsign.getRelative(fback).getRelative(fback)
                            .getRelative(SignUtil.getCounterClockWise(fback));
                case 5:
                    return bsign.getRelative(fback).getRelative(fback)
                            .getRelative(SignUtil.getClockWise(fback));
                default:
                    return null;

            }

        }

        @Override
        public boolean get(int pin) {
            Block block = getBlock(pin);
            if (block != null) {
                return block.isBlockIndirectlyPowered();
            } else {
                return false;
            }
        }

        @Override
        public void set(int pin, boolean value) {
            Block block = getBlock(pin);
            if (block != null) {
                ICUtil.setState(block, value);
            } else {
                return;
            }
        }

        @Override
        public boolean isTriggered(int pin) {
            Block block = getBlock(pin);
            if (block != null) {
                return BukkitUtil.toWorldVector(block).equals(source);
            } else {
                return false;
            }
        }

        @Override
        public boolean isValid(int pin) {
            Block block = getBlock(pin);
            if (block != null) {
                return block.getType() == Material.REDSTONE_WIRE;
            } else {
                return false;
            }
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
