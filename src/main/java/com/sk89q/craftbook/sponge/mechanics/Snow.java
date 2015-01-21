package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.block.BlockRandomTickEvent;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.core.util.CachePolicy;

public class Snow extends SpongeMechanic {

    @Subscribe
    public void onBlockTick(BlockRandomTickEvent event) {

        if(event.getBlock().getType() == BlockTypes.SNOW_LAYER) {

            //It's snow.
        }
    }

    @Override
    public String getName () {
        return "BetterSnow";
    }

    @Override
    public void onInitialize () {

        BlockTypes.SNOW_LAYER.setTickRandomly(true);
    }

    @Override
    public CachePolicy getCachePolicy () {
        return CachePolicy.NONE;
    }

}
