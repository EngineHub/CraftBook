package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import org.spongepowered.api.util.Direction;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.PinSet;
import com.sk89q.craftbook.sponge.util.SignUtil;

public class SISO extends PinSet {

    boolean isPowered;

    @Override
    public int getInputCount() {
        return 1;
    }

    @Override
    public int getOutputCount() {
        return 1;
    }

    @Override
    public void setInput(int inputId, boolean powered, IC ic) {
        isPowered = powered;
    }

    @Override
    public void setOutput(int outputId, boolean powered, IC ic) {

    }

    @Override
    public int getInputId(IC ic, Direction direction) {
        return 0;
    }

    @Override
    public boolean getInput(int inputId, IC ic) {
        return isPowered;
    }

    @Override
    public boolean getOutput(int outputId, IC ic) {
        return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock())).isPowered();
    }

    @Override
    public String getName() {
        return "SISO";
    }

}
