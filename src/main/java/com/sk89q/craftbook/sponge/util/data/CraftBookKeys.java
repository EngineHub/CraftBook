package com.sk89q.craftbook.sponge.util.data;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

public class CraftBookKeys {

    public static Key<MutableBoundedValue<Integer>> LAST_POWER = makeSingleKey(Integer.class, MutableBoundedValue.class, of("LastPower"));
    public static Key<Value<Long>> BLOCK_BAG = makeSingleKey(Long.class, Value.class, of("BlockBag"));
}
