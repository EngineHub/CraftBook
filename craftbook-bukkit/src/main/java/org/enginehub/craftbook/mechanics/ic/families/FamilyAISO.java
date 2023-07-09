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
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractChipState;
import org.enginehub.craftbook.mechanics.ic.AbstractICFamily;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.util.SignUtil;

/**
 * Handles detection for the single input single output family.
 *
 * @author sk89q
 */
public class FamilyAISO extends AbstractICFamily {

    @Override
    public ChipState detect(Location source, ChangedSign sign) {

        return new ChipStateAISO(source, sign);
    }

    @Override
    public ChipState detectSelfTriggered(Location source, ChangedSign sign) {

        return new ChipStateAISO(source, sign, true);
    }

    @Override
    public String getSuffix() {

        return "A";
    }

    public static class ChipStateAISO extends AbstractChipState {

        public ChipStateAISO(Location source, ChangedSign sign) {

            super(source, sign, false);
        }

        public ChipStateAISO(Location source, ChangedSign sign, boolean selfTriggered) {

            super(source, sign, selfTriggered);
        }

        @Override
        protected Block getBlock(int pin) {

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(sign.getBlock());
                case 1:
                    return SignUtil.getLeftBlock(sign.getBlock());
                case 2:
                    return SignUtil.getRightBlock(sign.getBlock());
                case 3:
                    BlockFace face = SignUtil.getBack(sign.getBlock());
                    return sign.getBlock().getRelative(face).getRelative(face);
                default:
                    return null;
            }

        }

        @Override
        public boolean getInput(int inputIndex) {

            for (int i = 0; i < getInputCount(); i++) {
                if (isValid(i) && get(i)) {
                    return true;
                }
            }
            return false;
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

            return 1;
        }

    }

    @Override
    public String getName() {
        return "AISO";
    }
}