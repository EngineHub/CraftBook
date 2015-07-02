package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;

public class SpongeRedstoneMechanicData extends SpongeMechanicData {

    public SpongeRedstoneMechanicData() {
    }

    public int lastCurrent;

    @Override
    public DataContainer toContainer() {

        DataContainer container = new MemoryDataContainer();
        container.set(DataQuery.of("lastCurrent"), lastCurrent);

        return container;
    }
}
