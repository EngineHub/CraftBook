package com.sk89q.craftbook.sponge.util.data.mutable;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableICData;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableNamespaceData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class NamespaceData extends AbstractSingleData<String, NamespaceData, ImmutableNamespaceData> {

    public NamespaceData() {
        this(null);
    }

    public NamespaceData(String value) {
        super(value, CraftBookKeys.NAMESPACE);
    }

    public Value<String> namespace() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.NAMESPACE, getValue());
    }

    @Override
    protected Value<String> getValueGetter() {
        return namespace();
    }

    @Override
    public Optional<NamespaceData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(NamespaceData.class).ifPresent((data) -> {
            NamespaceData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        });
        return Optional.of(this);
    }

    @Override
    public Optional<NamespaceData> from(DataContainer container) {
        if (container.contains(CraftBookKeys.NAMESPACE.getQuery())) {
            return Optional.of(new NamespaceData(container.getString(CraftBookKeys.NAMESPACE.getQuery()).get()));
        }

        return Optional.empty();
    }

    @Override
    public NamespaceData copy() {
        return new NamespaceData(getValue());
    }

    @Override
    public ImmutableNamespaceData asImmutable() {
        return new ImmutableNamespaceData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(CraftBookKeys.NAMESPACE, getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
