package com.sk89q.craftbook.sponge.mechanics.ics;

import org.spongepowered.api.util.Direction;

public abstract class PinSet {

    public abstract int getInputCount();

    public abstract int getOutputCount();

    public abstract void setInput(int inputId, boolean powered, IC ic);

    public abstract void setOutput(int outputId, boolean powered, IC ic);

    public abstract boolean getInput(int inputId, IC ic);

    public abstract boolean getOutput(int outputId, IC ic);

    public abstract int getInputId(IC ic, Direction direction);

    public abstract String getName();
}
