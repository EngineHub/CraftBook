package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import org.spongepowered.api.block.BlockTypes;
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

            // BlockProperty<?> prop = block.getState().getPropertyByName("powered").get();
            // block.replaceWith(block.getState().cycleProperty(prop));
            block.replaceWith(powered ? BlockTypes.REDSTONE_BLOCK : BlockTypes.STONE);
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
        return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock())).getType() == BlockTypes.REDSTONE_BLOCK;
    }

    @Override
    public String getName() {
        return "SISO";
    }

}
