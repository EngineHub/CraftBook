package com.sk89q.craftbook.sponge.mechanics.blockbags.data;

import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class EmbeddedBlockBagDataBuilder extends AbstractDataBuilder<EmbeddedBlockBagData> implements
        DataManipulatorBuilder<EmbeddedBlockBagData, ImmutableEmbeddedBlockBagData>  {

    public EmbeddedBlockBagDataBuilder() {
        super(EmbeddedBlockBagData.class, 1);
    }

    @Override
    public EmbeddedBlockBagData create() {
        return new EmbeddedBlockBagData();
    }

    @Override
    public Optional<EmbeddedBlockBagData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<EmbeddedBlockBagData> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(BlockBagManager.EMBEDDED_BLOCK_BAG.getQuery())) {
            return Optional.of(new EmbeddedBlockBagData(container.getSerializable(BlockBagManager.EMBEDDED_BLOCK_BAG.getQuery(),
                    EmbeddedBlockBag.class).get()));
        }

        return Optional.empty();
    }
}
