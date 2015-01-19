package com.sk89q.craftbook.core;

import org.spongepowered.api.block.BlockLoc;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.CachePolicy;
import com.sk89q.craftbook.core.util.CraftBookException;

public interface Mechanic {

    public String getName();

    public void onInitialize() throws CraftBookException;

    public MechanicData getData(BlockLoc location);

    public CachePolicy getCachePolicy();
}
