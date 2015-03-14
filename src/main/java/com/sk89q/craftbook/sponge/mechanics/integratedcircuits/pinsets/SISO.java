package com.sk89q.craftbook.sponge.mechanics.integratedcircuits.pinsets;

import org.spongepowered.api.util.Direction;

import com.sk89q.craftbook.sponge.mechanics.integratedcircuits.IC;
import com.sk89q.craftbook.sponge.mechanics.integratedcircuits.PinSet;

public class SISO extends PinSet {

    @Override
    public int getInputCount () {
        return 1;
    }

    @Override
    public int getOutputCount () {
        return 1;
    }

    @Override
    public void setInput(int inputId, boolean powered, IC ic) {

    }

    @Override
    public void setOutput(int outputId, boolean powered, IC ic) {

    }

    @Override
    public int getInputId (IC ic, Direction direction) {
        return 0;
    }

    @Override
    public boolean getInput (int inputId, IC ic) {
        return false;
    }

    @Override
    public boolean getOutput (int outputId, IC ic) {
        return false;
    }

    @Override
    public String getName () {
        return "SISO";
    }

}
