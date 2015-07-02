package com.sk89q.craftbook.sponge.util;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.MechanicDataCache;

public class SpongeDataCache extends MechanicDataCache {

    @Override
    protected <T extends MechanicData> T loadFromDisk(Class<T> clazz, String locationKey) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
        // TODO saving/loading.
    }

}
