package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.block.PoweredData;
import org.spongepowered.api.world.Location;

public abstract class PinSet {

    public abstract int getInputCount();

    public abstract int getOutputCount();

    public void setInput(int inputId, boolean powered, IC ic) {
        if(inputId == -1) return;
        ic.getPinStates()[inputId] = powered;
    }

    public void setOutput(int outputId, boolean powered, IC ic) {

        if(outputId == -1) return;

        if (getOutput(outputId, ic) != powered) {
            Location block = getPinLocation(outputId + getInputCount(), ic);

            if (block.getBlockType() != BlockTypes.LEVER) return; // Can't set this.

            if (powered)
                block.offer(block.getOrCreate(PoweredData.class).get());
            else
                block.remove(PoweredData.class);
        }
    }

    public int getPinForLocation(IC ic, Location location) {
        for(int i = 0; i < getInputCount() + getOutputCount(); i++)
            if(getPinLocation(i, ic).getBlockPosition().equals(location.getBlockPosition()))
                return i;
        return -1;
    }

    public boolean getInput(int inputId, IC ic) {
        if(inputId == -1) return false;
        return ic.getPinStates()[inputId];
    }

    public boolean getOutput(int outputId, IC ic) {
        if(outputId == -1) return false;
        return getPinLocation(getInputCount() + outputId, ic).getData(PoweredData.class).isPresent();
    }

    public boolean isValid(int id, IC ic) {
        BlockType type = getPinLocation(id, ic).getBlockType();

        return type == BlockTypes.REDSTONE_WIRE || type == BlockTypes.LEVER;
    }

    public abstract String getName();

    public abstract Location getPinLocation(int id, IC ic);
}
