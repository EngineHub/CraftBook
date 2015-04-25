package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulators.blocks.PoweredData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.PinSet;
import com.sk89q.craftbook.sponge.util.SignUtil;

public class SISO extends PinSet {

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
        ic.getPinStates()[inputId] = powered;
    }

    @Override
    public void setOutput(int outputId, boolean powered, IC ic) {

        if (getOutput(outputId, ic) != powered) {
            Location block = ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock()));

            if (block.getType() != BlockTypes.LEVER) return; // Can't set this.

            if (powered)
                block.offer(block.getOrCreate(PoweredData.class).get());
            else block.remove(PoweredData.class);
        }
    }

    @Override
    public int getInputId(IC ic, Direction direction) {
        return 0;
    }

    @Override
    public boolean getInput(int inputId, IC ic) {
        return ic.getPinStates()[inputId];
    }

    @Override
    public boolean getOutput(int outputId, IC ic) {
        return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock())).getData(PoweredData.class).isPresent();
    }

    @Override
    public String getName() {
        return "SISO";
    }

}
