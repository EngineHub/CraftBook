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

public class Family3I5O extends AbstractICFamily {

    @Override
    public ChipState detect(Location source, ChangedSign sign) {

        return new ChipState3I5O(source, sign);
    }

    @Override
    public ChipState detectSelfTriggered(Location source, ChangedSign sign) {

        return new ChipState3I5O(source, sign, true);
    }

    public static class ChipState3I5O extends AbstractChipState {

        public ChipState3I5O(Location source, ChangedSign sign) {

            super(source, sign, false);
        }

        public ChipState3I5O(Location source, ChangedSign sign, boolean selfTriggered) {

            super(source, sign, selfTriggered);
        }

        @Override
        protected Block getBlock(int pin) {

            BlockFace fback = SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock());
            Block backBlock = SignUtil.getBackBlock(CraftBookBukkitUtil.toSign(sign).getBlock()).getRelative(fback);
            Block farBlock = backBlock.getRelative(fback);

            switch (pin) {
                case 0:
                    return SignUtil.getFrontBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
                case 1:
                    return SignUtil.getLeftBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
                case 2:
                    return SignUtil.getRightBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
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
        public void set(int pin, boolean value) {

            Block block = getBlock(pin);
            if (block != null) {
                if(pin == 3 || pin == 4 || pin == 5)
                    ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock()), 2));
                else
                    ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock())));
            }
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

    @Override
    public String getName () {
        return "3I5O";
    }
}