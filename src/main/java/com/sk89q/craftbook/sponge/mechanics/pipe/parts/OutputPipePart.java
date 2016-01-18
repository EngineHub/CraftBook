package com.sk89q.craftbook.sponge.mechanics.pipe.parts;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.List;

public class OutputPipePart extends PipePart {

    @Override
    public boolean isValid(BlockState blockState) {
        return blockState.getType() == BlockTypes.PISTON;
    }

    @Override
    public List<Location> findValidOutputs(Location location, ItemStack itemStack, Direction inputSide) {

        List<Location> locations = new ArrayList<>();

        //The only location an output can pass to is the one it points at.
        locations.add(location.getRelative((Direction) location.get(Keys.DIRECTION).orElse(null)));

        return locations;
    }
}
