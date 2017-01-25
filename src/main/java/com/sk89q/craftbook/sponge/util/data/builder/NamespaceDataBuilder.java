package com.sk89q.craftbook.sponge.util.data.builder;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableNamespaceData;
import com.sk89q.craftbook.sponge.util.data.mutable.NamespaceData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class NamespaceDataBuilder extends AbstractDataBuilder<NamespaceData> implements DataManipulatorBuilder<NamespaceData, ImmutableNamespaceData> {

    public NamespaceDataBuilder() {
        super(NamespaceData.class, 1);
    }

    @Override
    public NamespaceData create() {
        return new NamespaceData();
    }

    @Override
    public Optional<NamespaceData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<NamespaceData> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(CraftBookKeys.NAMESPACE.getQuery())) {
            return Optional.of(new NamespaceData(container.getString(CraftBookKeys.NAMESPACE.getQuery()).get()));
        }

        return Optional.empty();
    }
}
