package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.service.persistence.DataBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;

import com.google.common.base.Optional;
import com.sk89q.craftbook.core.mechanics.MechanicData;

public abstract class SpongeMechanicData implements MechanicData, DataSerializable {

    public SpongeMechanicData() {
    }

    public static class SpongeMechanicDataBuilder<T extends SpongeMechanicData> implements DataBuilder<T> {

        @Override
        public Optional<T> build(DataView container) throws InvalidDataException {
            return null;
        }
    }
}
