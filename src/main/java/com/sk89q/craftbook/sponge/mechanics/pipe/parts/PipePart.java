package com.sk89q.craftbook.sponge.mechanics.pipe.parts;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.List;

public abstract class PipePart {

    public abstract boolean isValid(BlockState blockState);

    /**
     * Finds all locations that this pipe part is able to output items to.
     *
     * This does not need to check the validity of the target location block.
     *
     * @param location The location of this part.
     * @param itemStack The itemstack passed through.
     * @param inputSide The side that the input has come from.
     * @return A list of possible output locations.
     */
    public abstract List<Location> findValidOutputs(Location location, ItemStack itemStack, Direction inputSide);
}
