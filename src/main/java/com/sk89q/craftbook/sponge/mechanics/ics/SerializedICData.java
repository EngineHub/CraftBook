package com.sk89q.craftbook.sponge.mechanics.ics;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;

public abstract class SerializedICData implements DataSerializable {

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(DataQuery.of("ContentVersion"), this.getContentVersion());
    }
}
