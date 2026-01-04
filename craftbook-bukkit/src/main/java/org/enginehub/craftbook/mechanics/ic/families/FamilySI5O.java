/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.mechanics.ic.families;

import com.sk89q.worldedit.util.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractChipState;
import org.enginehub.craftbook.mechanics.ic.AbstractICFamily;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.SignUtil;

public class FamilySI5O extends AbstractICFamily {

    @Override
    public ChipState detect(Location source, BukkitChangedSign sign) {

        return new ChipStateSI5O(source, sign);
    }

    @Override
    public ChipState detectSelfTriggered(Location source, BukkitChangedSign sign) {

        return new ChipStateSI5O(source, sign, true);
    }

    public static class ChipStateSI5O extends AbstractChipState {

        public ChipStateSI5O(Location source, BukkitChangedSign sign) {

            super(source, sign, false);
        }

        public ChipStateSI5O(Location source, BukkitChangedSign sign, boolean selfTriggered) {

            super(source, sign, selfTriggered);
        }

        @Override
        protected Block getBlock(int pin) {

            BlockFace fback = SignUtil.getBack(sign.getBlock());
            Block backBlock = SignUtil.getBackBlock(sign.getBlock()).getRelative(fback);
            Block farBlock = backBlock.getRelative(fback);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(sign.getBlock());
                case 1:
                    return farBlock.getRelative(fback);
                case 2:
                    return farBlock.getRelative(SignUtil.getCounterClockWise(fback));
                case 3:
                    return farBlock.getRelative(SignUtil.getClockWise(fback));
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

            return get(outputIndex + 1);
        }

        @Override
        public void setOutput(int outputIndex, boolean value) {

            set(outputIndex + 1, value);
        }

        @Override
        public void set(int pin, boolean value) {

            Block block = getBlock(pin);
            if (block != null) {
                if (pin == 1 || pin == 2 || pin == 3)
                    ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(sign.getBlock()), 2));
                else
                    ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(sign.getBlock())));
            }
        }

        @Override
        public int getInputCount() {

            return 1;
        }

        @Override
        public int getOutputCount() {

            return 5;
        }

    }

    @Override
    public String getName() {
        return "SI5O";
    }
}
