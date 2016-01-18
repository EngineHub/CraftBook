package com.sk89q.craftbook.sponge.mechanics.pipe.parts;

import com.sk89q.craftbook.sponge.mechanics.pipe.PipePart;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.List;

public class PassthroughPipePart extends PipePart {

    @Override
    public boolean isValid(BlockState blockState) {
        return blockState.getType() == BlockTypes.GLASS;
    }

    @Override
    public List<Location> findValidOutputs(Location location, ItemStack itemStack, Direction inputSide) {
        return BlockUtil.getAdjacentExcept(location, inputSide.getOpposite());
    }
}
