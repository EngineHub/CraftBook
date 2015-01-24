package com.sk89q.craftbook.core.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.craftbook.core.mechanics.MechanicData;

public abstract class MechanicDataCache {

    private LoadingCache<String, MechanicData> mechanicData = CacheBuilder.newBuilder().maximumSize(250)
            .build(new CacheLoader<String, MechanicData>() {
                @Override
                public MechanicData load(String locationKey) {

                    return loadFromDisk(locationKey);
                }
            });

    protected abstract MechanicData loadFromDisk(String locationKey);

    public MechanicData getMechanicData(String locationKey) {

        if(locationKey == null) return null;

        return mechanicData.getUnchecked(locationKey);
    }
}