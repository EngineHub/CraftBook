package com.sk89q.craftbook.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.sk89q.craftbook.core.mechanics.MechanicData;

import java.util.concurrent.TimeUnit;

public abstract class MechanicDataCache {

    private Cache<String, MechanicData> mechanicData = CacheBuilder.newBuilder().maximumSize(250).expireAfterAccess(10, TimeUnit.MINUTES).removalListener(new RemovalListener<String, MechanicData>() {
        @Override
        public void onRemoval(RemovalNotification<String, MechanicData> notification) {
            MechanicData value = notification.getValue();
            saveToDisk((Class<MechanicData>) value.getClass(), notification.getKey(), value);
        }
    }).build();

    protected abstract <T extends MechanicData> T loadFromDisk(Class<T> clazz, String locationKey);

    protected abstract void saveToDisk(Class<MechanicData> clazz, String locationKey, MechanicData data);

    public <T extends MechanicData> T getMechanicData(Class<T> clazz, String locationKey) {

        if (locationKey == null) return null;

        T data = (T) mechanicData.getIfPresent(locationKey);

        if (data == null || !clazz.isInstance(data)) {
            data = loadFromDisk(clazz, locationKey);
            mechanicData.put(locationKey, data);
        }

        return data;
    }

    public void clearAll() {
        mechanicData.invalidateAll();
    }
}
