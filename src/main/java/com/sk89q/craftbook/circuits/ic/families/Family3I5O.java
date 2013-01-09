package com.sk89q.craftbook.circuits.ic.families;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractChipState;
import com.sk89q.craftbook.circuits.ic.AbstractICFamily;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;

public class Family3I5O extends AbstractICFamily {

    @Override
    public ChipState detect(BlockWorldVector source, ChangedSign sign) {

        return new ChipState3I5O(source, sign);
    }

    @Override
    public ChipState detectSelfTriggered(BlockWorldVector source, ChangedSign sign) {

        return new ChipState3I5O(source, sign, true);
    }

    public static class ChipState3I5O extends AbstractChipState {

        public ChipState3I5O(BlockWorldVector source, ChangedSign sign) {

            super(source, sign, false);
        }

        public ChipState3I5O(BlockWorldVector source, ChangedSign sign, boolean selfTriggered) {

            super(source, sign, selfTriggered);
        }

        @Override
        protected Block getBlock(int pin) {

            BlockFace fback = SignUtil.getBack(BukkitUtil.toSign(sign).getBlock());
            Block backBlock = SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock()).getRelative(fback);
            Block farBlock = backBlock.getRelative(fback);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(BukkitUtil.toSign(sign).getBlock());
                case 1:
                    return SignUtil.getLeftBlock(BukkitUtil.toSign(sign).getBlock());
                case 2:
                    return SignUtil.getRightBlock(BukkitUtil.toSign(sign).getBlock());
                case 3:
                    return farBlock.getRelative(fback);
                case 4:
                    return farBlock.getRelative(SignUtil.getCounterClockWise(fback));
                case 5:
                    return farBlock.getRelative(SignUtil.getClockWise(fback));
                case 6:
                    return backBlock.getRelative(SignUtil.getCounterClockWise(fback));
                case 7:
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
        public int getInputCount() {

            return 3;
        }

        @Override
        public int getOutputCount() {

            return 5;
        }

    }

}
