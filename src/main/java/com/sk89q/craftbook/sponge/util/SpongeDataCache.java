package com.sk89q.craftbook.sponge.util;

import com.sk89q.craftbook.core.util.MechanicDataCache;

public class SpongeDataCache extends MechanicDataCache {

    @Override
    protected SpongeMechanicData loadFromDisk(String locationKey) {
        return null;
        // return CraftBookPlugin.<CraftBookPlugin>inst().game.getServiceManager().;
    }

}
