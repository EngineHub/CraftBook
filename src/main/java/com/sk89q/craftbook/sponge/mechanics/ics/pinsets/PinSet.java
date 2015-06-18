package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import org.spongepowered.api.util.Direction;

public interface PinSet {

    int getInputCount();

    int getOutputCount();

    void setInput(int inputId, boolean powered, IC ic);

    void setOutput(int outputId, boolean powered, IC ic);

    boolean getInput(int inputId, IC ic);

    boolean getOutput(int outputId, IC ic);

    int getInputId(IC ic, Direction direction);

    String getName();
}
