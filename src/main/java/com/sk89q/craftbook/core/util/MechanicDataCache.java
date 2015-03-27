package com.sk89q.craftbook.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.craftbook.core.mechanics.MechanicData;

public abstract class MechanicDataCache {

    private Cache<String, MechanicData> mechanicData = CacheBuilder.newBuilder().maximumSize(250).build();

    protected abstract <T extends MechanicData> T loadFromDisk(Class<T> clazz, String locationKey);

    public <T extends MechanicData> T getMechanicData(Class<T> clazz, String locationKey) {

        if (locationKey == null) return null;

        T data = (T) mechanicData.getIfPresent(locationKey);

        if(data == null || !clazz.isInstance(data)) {
            data = loadFromDisk(clazz, locationKey);
            mechanicData.put(locationKey, data);
        }

        return data;
    }
}
