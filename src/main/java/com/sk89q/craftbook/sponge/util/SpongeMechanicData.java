package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.service.persistence.DataSerializable;
import org.spongepowered.api.service.persistence.DataSerializableBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.data.DataView;

import com.google.common.base.Optional;
import com.sk89q.craftbook.core.mechanics.MechanicData;

public abstract class SpongeMechanicData implements MechanicData, DataSerializable {

    public SpongeMechanicData() {
    }

    public static class SpongeMechanicDataBuilder<T extends SpongeMechanicData> implements DataSerializableBuilder<T> {

        @Override
        public Optional<T> build(DataView container) throws InvalidDataException {
            return null;
        }
    }
}
