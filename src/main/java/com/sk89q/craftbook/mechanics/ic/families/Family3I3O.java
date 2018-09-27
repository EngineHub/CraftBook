// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.ic.families;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractChipState;
import com.sk89q.craftbook.mechanics.ic.AbstractICFamily;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.util.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Handles detection for the triple-input triple-output family.
 *
 * @author robhol
 */
public class Family3I3O extends AbstractICFamily {

    @Override
    public ChipState detect(Location source, ChangedSign sign) {

        return new ChipState3I3O(source, sign);
    }

    @Override
    public ChipState detectSelfTriggered(Location source, ChangedSign sign) {

        return new ChipState3I3O(source, sign, true);
    }

    public static class ChipState3I3O extends AbstractChipState {

        public ChipState3I3O(Location source, ChangedSign sign) {

            super(source, sign, false);
        }

        public ChipState3I3O(Location source, ChangedSign sign, boolean selfTriggered) {

            super(source, sign, selfTriggered);
        }

        @Override
        protected Block getBlock(int pin) {

            BlockFace fback = SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock());
            Block backBlock = CraftBookBukkitUtil.toSign(sign).getBlock().getRelative(fback, 2);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
                case 1:
                    return SignUtil.getLeftBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
                case 2:
                    return SignUtil.getRightBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
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
        public void set(int pin, boolean value) {

            Block block = getBlock(pin);
            if (block != null) {
                ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock())));
            }
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

    @Override
    public String getName () {
        return "3I3O";
    }
}
