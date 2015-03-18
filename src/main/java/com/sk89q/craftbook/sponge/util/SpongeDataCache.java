package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.service.persistence.DataSerializable;
import org.spongepowered.api.service.persistence.DataSource;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.data.DataContainer;

import com.google.common.base.Optional;
import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.MechanicDataCache;

public class SpongeDataCache extends MechanicDataCache implements DataSource {

    @Override
    protected <T extends MechanicData> T loadFromDisk(String locationKey) {
        return null;
        // return CraftBookPlugin.<CraftBookPlugin>inst().game.getServiceManager().;
    }

    @Override
    public <T extends DataSerializable> Optional<T> deserialize(Class<T> clazz) throws InvalidDataException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<DataContainer> deserialize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void serialize(DataSerializable section) throws InvalidDataException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

}
