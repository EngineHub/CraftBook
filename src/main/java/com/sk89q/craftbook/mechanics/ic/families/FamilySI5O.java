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

public class FamilySI5O extends AbstractICFamily {

    @Override
    public ChipState detect(Location source, ChangedSign sign) {

        return new ChipStateSI5O(source, sign);
    }

    @Override
    public ChipState detectSelfTriggered(Location source, ChangedSign sign) {

        return new ChipStateSI5O(source, sign, true);
    }

    public static class ChipStateSI5O extends AbstractChipState {

        public ChipStateSI5O(Location source, ChangedSign sign) {

            super(source, sign, false);
        }

        public ChipStateSI5O(Location source, ChangedSign sign, boolean selfTriggered) {

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
                if(pin == 1 || pin == 2 || pin == 3)
                    ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock()), 2));
                else
                    ICUtil.setState(block, value, icBlock.getRelative(SignUtil.getBack(CraftBookBukkitUtil.toSign(sign).getBlock())));
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
    public String getName () {
        return "SI5O";
    }
}
