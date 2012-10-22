package com.sk89q.craftbook.ic.families;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractChipState;
import com.sk89q.craftbook.ic.AbstractICFamily;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;

public class FamilySI5O extends AbstractICFamily {

    @Override
    public ChipState detect(BlockWorldVector source, Sign sign) {

        return new ChipStateSI5O(source, sign);
    }

	@Override
	public ChipState detectSelfTriggered(BlockWorldVector source, Sign sign) {

		return new ChipStateSI5O(source, sign, true);
	}


	public static class ChipStateSI5O extends AbstractChipState {

        public ChipStateSI5O(BlockWorldVector source, Sign sign) {

            super(source, sign, false);
        }

	    public ChipStateSI5O(BlockWorldVector source, Sign sign, boolean selfTriggered) {

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
        public int getInputCount() {

            return 1;
        }

        @Override
        public int getOutputCount() {

            return 5;
        }

    }
}
