package com.sk89q.craftbook.sponge.mechanics.pipe;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.List;

public abstract class PipePart {

    public abstract boolean isValid(BlockState blockState);

    public abstract List<Location> findValidOutputs(Location location, ItemStack itemStack, Direction inputSide);
}
