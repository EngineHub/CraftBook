package com.sk89q.craftbook.sponge.util.data.builder;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableBlockBagData;
import com.sk89q.craftbook.sponge.util.data.mutable.BlockBagData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class BlockBagDataManipulatorBuilder implements DataManipulatorBuilder<BlockBagData, ImmutableBlockBagData> {

    @Override
    public BlockBagData create() {
        return new BlockBagData();
    }

    @Override
    public Optional<BlockBagData> createFrom(DataHolder dataHolder) {
        return Optional.of(dataHolder.get(BlockBagData.class).orElse(new BlockBagData()));
    }

    @Override
    public Optional<BlockBagData> build(DataView container) throws InvalidDataException {
        if (container.contains(CraftBookKeys.BLOCK_BAG)) {
            final long blockBag = container.getLong(CraftBookKeys.BLOCK_BAG.getQuery()).get();
            return Optional.of(new BlockBagData(blockBag));
        }
        return Optional.empty();
    }
}
