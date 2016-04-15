/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.sponge.CraftBookPlugin;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public abstract class MechanicDataCache {

    private Cache<String, MechanicData> mechanicData = CacheBuilder.newBuilder().maximumSize(250).expireAfterAccess(10, TimeUnit.MINUTES).removalListener(new RemovalListener<String, MechanicData>() {
        @Override
        public void onRemoval(@Nonnull RemovalNotification<String, MechanicData> notification) {
            MechanicData value = notification.getValue();
            if(value == null)
                return;
            saveToDisk((Class<MechanicData>) value.getClass(), notification.getKey(), value);
        }
    }).build();

    protected abstract <T extends MechanicData> T loadFromDisk(Class<T> clazz, String locationKey);

    protected abstract <T extends MechanicData> void saveToDisk(Class<T> clazz, String locationKey, T data);

    public <T extends MechanicData> T getMechanicData(Class<T> clazz, String locationKey) {
        if (locationKey == null) return null;

        Object data = null;
        try {
            data = mechanicData.getIfPresent(locationKey);
        } catch(Throwable e) {
            CraftBookAPI.<CraftBookPlugin>inst().getLogger().error("Failed to load some data: " + locationKey, e);
        }

        if (data == null || !clazz.isInstance(data)) {
            data = loadFromDisk(clazz, locationKey);
            mechanicData.put(locationKey, (MechanicData) data);
        }

        return (T) data;
    }

    public void clearAll() {
        mechanicData.invalidateAll();
    }
}
