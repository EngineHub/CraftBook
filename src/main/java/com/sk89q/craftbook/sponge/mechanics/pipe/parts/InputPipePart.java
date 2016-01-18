package com.sk89q.craftbook.sponge.mechanics.pipe.parts;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

public class InputPipePart extends PassthroughPipePart {

    @Override
    public boolean isValid(BlockState blockState) {
        return blockState.getType() == BlockTypes.STICKY_PISTON;
    }
}
