package com.sk89q.craftbook.sponge.util.data;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

public class CraftBookKeys {

    public static Key<MutableBoundedValue<Integer>> LAST_POWER = makeSingleKey(Integer.class, MutableBoundedValue.class, of("LastPower"));
}
