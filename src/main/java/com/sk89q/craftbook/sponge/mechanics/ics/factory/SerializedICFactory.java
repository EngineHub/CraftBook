package com.sk89q.craftbook.sponge.mechanics.ics.factory;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;

public abstract class SerializedICFactory<T extends IC, D extends SerializedICData> extends AbstractDataBuilder<D> implements ICFactory<T> {

    private Class<D> requiredClass;

    public SerializedICFactory(Class<D> requiredClass, int supportedVersion) {
        super(requiredClass, supportedVersion);

        this.requiredClass = requiredClass;
    }

    public Class<D> getRequiredClass() {
        return this.requiredClass;
    }

    public abstract void setData(T ic, D data);

    public abstract D getData(T ic);
}
