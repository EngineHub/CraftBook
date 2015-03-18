package com.sk89q.craftbook.core.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.craftbook.core.mechanics.MechanicData;

public abstract class MechanicDataCache {

    //TODO make a proper type-safe loader.

    private LoadingCache<String, MechanicData> mechanicData = CacheBuilder.newBuilder().maximumSize(250).build(new CacheLoader<String, MechanicData>() {
        @Override
        public MechanicData load(String locationKey) {

            return loadFromDisk(locationKey);
        }
    });

    protected abstract <T extends MechanicData> T loadFromDisk(String locationKey);

    public <T extends MechanicData> T getMechanicData(String locationKey) {

        if (locationKey == null) return null;

        return (T) mechanicData.getUnchecked(locationKey);
    }
}
