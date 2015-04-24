package com.sk89q.craftbook.sponge.mechanics.ics;

import java.lang.reflect.Field;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.world.Location;

public abstract class IC implements DataSerializable {

    public ICType<? extends IC> type;
    public Location block;
    public boolean[] pinstates;

    public IC(ICType<? extends IC> type, Location block) {
        this.type = type;
        this.block = block;
    }

    public String getPinSetName() {
        return type.getDefaultPinSet();
    }

    public PinSet getPinSet() {
        return ICSocket.PINSETS.get(getPinSetName());
    }

    public void load() {

        PinSet set = getPinSet();
        pinstates = new boolean[set.getInputCount()]; // Just input for now.
    }

    public Location getBlock() {
        return block;
    }

    public ICType<? extends IC> getType() {
        return type;
    }

    public abstract void trigger();

    public boolean[] getPinStates() {
        return pinstates;
    }

    @Override
    public DataContainer toContainer() {

        DataContainer container = new MemoryDataContainer();

        for (Field field : this.getClass().getFields()) {
            try {
                container.set(DataQuery.of(field.getName()), field.get(this));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return container;
    }
}
