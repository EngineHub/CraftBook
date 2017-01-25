package com.sk89q.craftbook.sponge.util.data.immutable;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.NamespaceData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableNamespaceData extends AbstractImmutableSingleData<String, ImmutableNamespaceData, NamespaceData> {

    public ImmutableNamespaceData() {
        this(null);
    }

    public ImmutableNamespaceData(String value) {
        super(value, CraftBookKeys.NAMESPACE);
    }

    @Override
    protected ImmutableValue<String> getValueGetter() {
        return Sponge.getRegistry().getValueFactory()
                .createValue(CraftBookKeys.NAMESPACE, getValue())
                .asImmutable();
    }

    @Override
    public NamespaceData asMutable() {
        return new NamespaceData(getValue());
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
